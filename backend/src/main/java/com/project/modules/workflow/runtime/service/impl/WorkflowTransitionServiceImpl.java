package com.project.modules.workflow.runtime.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.exception.DomainError;
import com.project.common.exception.DomainException;
import com.project.common.idempotency.IdempotencySupport;
import com.project.modules.workflow.runtime.dto.WorkflowStepCompleteRequest;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import com.project.modules.workflow.runtime.entity.WorkflowStepRecord;
import com.project.modules.workflow.runtime.entity.WorkflowTemplateNode;
import com.project.modules.workflow.runtime.mapper.WorkflowInstanceMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowStepRecordMapper;
import com.project.modules.workflow.runtime.model.WorkflowInstanceStatus;
import com.project.modules.workflow.runtime.model.WorkflowStepStatus;
import com.project.modules.workflow.runtime.service.WorkflowRuntimeAccessService;
import com.project.modules.workflow.runtime.service.WorkflowTransitionService;
import com.project.modules.workflow.runtime.vo.WorkflowNextStepVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepCompleteVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class WorkflowTransitionServiceImpl implements WorkflowTransitionService {

    private final WorkflowRuntimeAccessService accessService;
    private final WorkflowInstanceMapper instanceMapper;
    private final WorkflowStepRecordMapper stepRecordMapper;
    private final IdempotencySupport idempotencySupport;
    private final ObjectMapper objectMapper;

    public WorkflowTransitionServiceImpl(
            WorkflowRuntimeAccessService accessService,
            WorkflowInstanceMapper instanceMapper,
            WorkflowStepRecordMapper stepRecordMapper,
            IdempotencySupport idempotencySupport,
            ObjectMapper objectMapper
    ) {
        this.accessService = accessService;
        this.instanceMapper = instanceMapper;
        this.stepRecordMapper = stepRecordMapper;
        this.idempotencySupport = idempotencySupport;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowStepCompleteVO completeStep(
            Long userId,
            Long instanceId,
            Long nodeId,
            WorkflowStepCompleteRequest request,
            String idempotencyKey
    ) {
        WorkflowInstance instance = accessService.requireOwnedInstance(userId, instanceId);
        String requestId = idempotencySupport.normalizeKey(idempotencyKey);
        String requestHash = requestId == null ? null : completionFingerprint(nodeId, request);
        WorkflowStepCompleteVO replay = findReplay(instanceId, requestId, requestHash);
        if (replay != null) {
            return replay;
        }

        if (WorkflowInstanceStatus.FINISHED.name().equals(instance.getStatus())) {
            throw new DomainException(DomainError.WORKFLOW_ALREADY_FINISHED);
        }
        List<WorkflowTemplateNode> nodes = accessService.listEnabledNodes(instance.getTemplateId());
        WorkflowTemplateNode currentNode = nodes.stream()
                .filter(node -> Objects.equals(node.getId(), nodeId))
                .findFirst()
                .orElseThrow(() -> new DomainException(DomainError.WORKFLOW_NOT_FOUND,
                        "Workflow node does not belong to this template or is disabled"));
        if (!Objects.equals(instance.getCurrentNodeId(), nodeId)) {
            Long completed = stepRecordMapper.selectCount(new LambdaQueryWrapper<WorkflowStepRecord>()
                    .eq(WorkflowStepRecord::getInstanceId, instanceId)
                    .eq(WorkflowStepRecord::getNodeId, nodeId)
                    .eq(WorkflowStepRecord::getStatus, WorkflowStepStatus.COMPLETED.name()));
            if (completed > 0) {
                throw new DomainException(DomainError.WORKFLOW_STATE_CONFLICT,
                        "Workflow node was already completed by another request");
            }
            throw new DomainException(DomainError.WORKFLOW_NODE_NOT_CURRENT);
        }

        WorkflowTemplateNode nextNode = findNextNode(nodes, nodeId);
        WorkflowNextStepVO nextStep = nextNode == null
                ? WorkflowNextStepVO.builder().whetherFinished(true).build()
                : WorkflowNextStepVO.builder()
                        .nextNodeId(nextNode.getId())
                        .nextNodeName(nextNode.getNodeName())
                        .nextTip(currentNode.getNextTip())
                        .whetherFinished(false)
                        .build();
        int completedCount = Math.toIntExact(stepRecordMapper.selectCount(
                new LambdaQueryWrapper<WorkflowStepRecord>()
                        .eq(WorkflowStepRecord::getInstanceId, instanceId)
                        .eq(WorkflowStepRecord::getStatus, WorkflowStepStatus.COMPLETED.name())
        ));
        BigDecimal progress = calculateProgress(completedCount + 1, nodes.size());
        LocalDateTime now = LocalDateTime.now();
        String nextStatus = nextNode == null
                ? WorkflowInstanceStatus.FINISHED.name()
                : WorkflowInstanceStatus.RUNNING.name();

        int advanced = instanceMapper.advanceCurrentNode(
                instanceId,
                userId,
                nodeId,
                instance.getLockVersion() == null ? 0L : instance.getLockVersion(),
                nextNode == null ? null : nextNode.getId(),
                nextStatus,
                nextNode == null ? now : null,
                progress,
                now
        );
        if (advanced != 1) {
            replay = findReplay(instanceId, requestId, requestHash);
            if (replay != null) {
                return replay;
            }
            throw new DomainException(DomainError.WORKFLOW_STATE_CONFLICT);
        }

        WorkflowStepCompleteVO response = WorkflowStepCompleteVO.builder()
                .instanceId(instanceId)
                .completedNodeId(nodeId)
                .progress(progress)
                .nextStep(nextStep)
                .build();
        WorkflowStepRecord record = new WorkflowStepRecord();
        record.setInstanceId(instanceId);
        record.setNodeId(nodeId);
        record.setUserId(userId);
        record.setInputContent(request.getInputContent());
        record.setOutputContent(request.getOutputContent());
        record.setStatus(WorkflowStepStatus.COMPLETED.name());
        record.setDurationSeconds(request.getDurationSeconds());
        record.setCompletedAt(now);
        record.setNextSuggestion(currentNode.getNextTip());
        record.setCompletionRequestId(requestId);
        record.setCompletionRequestHash(requestHash);
        record.setCompletionResponseJson(writeResponse(response));
        record.setCreateTime(now);
        record.setUpdateTime(now);
        record.setIsDeleted(0);
        try {
            stepRecordMapper.insert(record);
        } catch (DuplicateKeyException ex) {
            throw new DomainException(DomainError.WORKFLOW_STATE_CONFLICT,
                    "A canonical record already exists for this workflow node");
        }
        return response;
    }

    private WorkflowStepCompleteVO findReplay(Long instanceId, String requestId, String requestHash) {
        if (requestId == null) {
            return null;
        }
        WorkflowStepRecord existing = stepRecordMapper.selectByCompletionRequest(instanceId, requestId);
        if (existing == null) {
            return null;
        }
        idempotencySupport.requireSameRequest(existing.getCompletionRequestHash(), requestHash);
        try {
            return objectMapper.readValue(existing.getCompletionResponseJson(), WorkflowStepCompleteVO.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Stored workflow completion response is invalid", ex);
        }
    }

    private String completionFingerprint(Long nodeId, WorkflowStepCompleteRequest request) {
        Map<String, Object> shape = new LinkedHashMap<>();
        shape.put("nodeId", nodeId);
        shape.put("inputContent", request.getInputContent());
        shape.put("outputContent", request.getOutputContent());
        shape.put("durationSeconds", request.getDurationSeconds());
        return idempotencySupport.fingerprint(shape);
    }

    private String writeResponse(WorkflowStepCompleteVO response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to persist workflow completion response", ex);
        }
    }

    private BigDecimal calculateProgress(int completedCount, int totalCount) {
        if (totalCount <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(completedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
    }

    private WorkflowTemplateNode findNextNode(List<WorkflowTemplateNode> nodes, Long nodeId) {
        for (int i = 0; i < nodes.size(); i++) {
            if (Objects.equals(nodes.get(i).getId(), nodeId)) {
                return i + 1 < nodes.size() ? nodes.get(i + 1) : null;
            }
        }
        return null;
    }
}
