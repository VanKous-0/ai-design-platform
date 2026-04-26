package com.project.modules.workflow.runtime.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.project.common.exception.BusinessException;
import com.project.modules.workflow.runtime.dto.WorkflowInstanceCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepCompleteRequest;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import com.project.modules.workflow.runtime.entity.WorkflowStepRecord;
import com.project.modules.workflow.runtime.entity.WorkflowTemplate;
import com.project.modules.workflow.runtime.entity.WorkflowTemplateNode;
import com.project.modules.workflow.runtime.mapper.WorkflowInstanceMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowStepRecordMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateNodeMapper;
import com.project.modules.workflow.runtime.service.WorkflowInstanceService;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceListVO;
import com.project.modules.workflow.runtime.vo.WorkflowNextStepVO;
import com.project.modules.workflow.runtime.vo.WorkflowProgressVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepCompleteVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepRecordVO;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateNodeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService {

    private static final int STATUS_ENABLED = 1;
    private static final String INSTANCE_RUNNING = "RUNNING";
    private static final String INSTANCE_FINISHED = "FINISHED";
    private static final String STEP_COMPLETED = "COMPLETED";

    private final WorkflowTemplateMapper templateMapper;
    private final WorkflowTemplateNodeMapper nodeMapper;
    private final WorkflowInstanceMapper instanceMapper;
    private final WorkflowStepRecordMapper stepRecordMapper;

    public WorkflowInstanceServiceImpl(
            WorkflowTemplateMapper templateMapper,
            WorkflowTemplateNodeMapper nodeMapper,
            WorkflowInstanceMapper instanceMapper,
            WorkflowStepRecordMapper stepRecordMapper
    ) {
        this.templateMapper = templateMapper;
        this.nodeMapper = nodeMapper;
        this.instanceMapper = instanceMapper;
        this.stepRecordMapper = stepRecordMapper;
    }

    @Override
    public WorkflowInstanceDetailVO createInstance(Long userId, WorkflowInstanceCreateRequest request) {
        WorkflowTemplate template = getEnabledTemplate(request.getTemplateId());
        List<WorkflowTemplateNode> nodes = listEnabledNodes(template.getId());
        if (nodes.isEmpty()) {
            throw new BusinessException("Workflow template has no enabled nodes");
        }

        LocalDateTime now = LocalDateTime.now();
        WorkflowInstance instance = new WorkflowInstance();
        instance.setTemplateId(template.getId());
        instance.setUserId(userId);
        instance.setTitle(hasText(request.getTitle()) ? request.getTitle().trim() : template.getName());
        instance.setCurrentNodeId(nodes.get(0).getId());
        instance.setStatus(INSTANCE_RUNNING);
        instance.setStartTime(now);
        instance.setProgress(BigDecimal.ZERO);
        instance.setCreateTime(now);
        instance.setUpdateTime(now);
        instance.setIsDeleted(0);
        instanceMapper.insert(instance);
        return toDetailVO(instance);
    }

    @Override
    public List<WorkflowInstanceListVO> listMyInstances(Long userId) {
        return instanceMapper.selectList(new LambdaQueryWrapper<WorkflowInstance>()
                        .eq(WorkflowInstance::getUserId, userId)
                        .orderByDesc(WorkflowInstance::getCreateTime)
                        .orderByDesc(WorkflowInstance::getId))
                .stream()
                .map(this::toListVO)
                .toList();
    }

    @Override
    public WorkflowInstanceDetailVO getMyInstance(Long userId, Long instanceId) {
        return toDetailVO(getOwnedInstance(userId, instanceId));
    }

    @Override
    public WorkflowProgressVO getProgress(Long userId, Long instanceId) {
        WorkflowInstance instance = getOwnedInstance(userId, instanceId);
        List<WorkflowTemplateNode> nodes = listEnabledNodes(instance.getTemplateId());
        int completedCount = countCompletedNodes(instance.getId());
        BigDecimal progress = calculateProgress(completedCount, nodes.size());
        WorkflowTemplateNode currentNode = findNode(instance.getCurrentNodeId());
        return WorkflowProgressVO.builder()
                .instanceId(instance.getId())
                .totalNodeCount(nodes.size())
                .completedNodeCount(completedCount)
                .progress(progress)
                .status(instance.getStatus())
                .currentNodeId(instance.getCurrentNodeId())
                .currentNodeName(currentNode == null ? null : currentNode.getNodeName())
                .build();
    }

    @Override
    public WorkflowNextStepVO getNextStep(Long userId, Long instanceId) {
        WorkflowInstance instance = getOwnedInstance(userId, instanceId);
        if (INSTANCE_FINISHED.equals(instance.getStatus()) || instance.getCurrentNodeId() == null) {
            return finishedNextStep();
        }
        WorkflowTemplateNode currentNode = findNode(instance.getCurrentNodeId());
        return WorkflowNextStepVO.builder()
                .nextNodeId(instance.getCurrentNodeId())
                .nextNodeName(currentNode == null ? null : currentNode.getNodeName())
                .nextTip(currentNode == null ? null : currentNode.getNextTip())
                .whetherFinished(false)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowStepCompleteVO completeStep(Long userId, Long instanceId, Long nodeId, WorkflowStepCompleteRequest request) {
        WorkflowInstance instance = getOwnedInstance(userId, instanceId);
        if (INSTANCE_FINISHED.equals(instance.getStatus())) {
            throw new BusinessException("Workflow instance has been finished");
        }

        List<WorkflowTemplateNode> nodes = listEnabledNodes(instance.getTemplateId());
        WorkflowTemplateNode currentNode = findNodeInList(nodes, nodeId);
        if (currentNode == null) {
            throw new BusinessException("Workflow node does not belong to this template or is disabled");
        }
        if (!Objects.equals(instance.getCurrentNodeId(), nodeId)) {
            throw new BusinessException("Only current workflow node can be completed");
        }

        WorkflowTemplateNode nextNode = findNextNode(nodes, nodeId);
        WorkflowNextStepVO nextStep = nextNode == null ? finishedNextStep() : WorkflowNextStepVO.builder()
                .nextNodeId(nextNode.getId())
                .nextNodeName(nextNode.getNodeName())
                .nextTip(currentNode.getNextTip())
                .whetherFinished(false)
                .build();

        saveOrUpdateStepRecord(userId, instance, nodeId, request, currentNode.getNextTip());

        int completedCount = countCompletedNodes(instance.getId());
        BigDecimal progress = calculateProgress(completedCount, nodes.size());
        LocalDateTime now = LocalDateTime.now();
        instance.setCurrentNodeId(nextNode == null ? null : nextNode.getId());
        instance.setStatus(nextNode == null ? INSTANCE_FINISHED : INSTANCE_RUNNING);
        instance.setFinishTime(nextNode == null ? now : null);
        instance.setProgress(progress);
        instance.setUpdateTime(now);
        instanceMapper.updateById(instance);

        return WorkflowStepCompleteVO.builder()
                .instanceId(instance.getId())
                .completedNodeId(nodeId)
                .progress(progress)
                .nextStep(nextStep)
                .build();
    }

    private WorkflowTemplate getEnabledTemplate(Long templateId) {
        WorkflowTemplate template = templateMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getId, templateId)
                .eq(WorkflowTemplate::getStatus, STATUS_ENABLED)
                .last("limit 1"));
        if (template == null) {
            throw new BusinessException("Workflow template does not exist or is disabled");
        }
        return template;
    }

    private WorkflowInstance getOwnedInstance(Long userId, Long instanceId) {
        WorkflowInstance instance = instanceMapper.selectOne(new LambdaQueryWrapper<WorkflowInstance>()
                .eq(WorkflowInstance::getId, instanceId)
                .eq(WorkflowInstance::getUserId, userId)
                .last("limit 1"));
        if (instance == null) {
            throw new BusinessException("Workflow instance does not exist or you have no permission");
        }
        return instance;
    }

    private List<WorkflowTemplateNode> listEnabledNodes(Long templateId) {
        return nodeMapper.selectList(new LambdaQueryWrapper<WorkflowTemplateNode>()
                .eq(WorkflowTemplateNode::getTemplateId, templateId)
                .eq(WorkflowTemplateNode::getStatus, STATUS_ENABLED)
                .orderByAsc(WorkflowTemplateNode::getSortOrder)
                .orderByAsc(WorkflowTemplateNode::getId));
    }

    private void saveOrUpdateStepRecord(
            Long userId,
            WorkflowInstance instance,
            Long nodeId,
            WorkflowStepCompleteRequest request,
            String nextSuggestion
    ) {
        LocalDateTime now = LocalDateTime.now();
        WorkflowStepRecord existing = stepRecordMapper.selectOne(new LambdaQueryWrapper<WorkflowStepRecord>()
                .eq(WorkflowStepRecord::getInstanceId, instance.getId())
                .eq(WorkflowStepRecord::getNodeId, nodeId)
                .last("limit 1"));
        if (existing == null) {
            WorkflowStepRecord record = new WorkflowStepRecord();
            record.setInstanceId(instance.getId());
            record.setNodeId(nodeId);
            record.setUserId(userId);
            record.setInputContent(request.getInputContent());
            record.setOutputContent(request.getOutputContent());
            record.setStatus(STEP_COMPLETED);
            record.setDurationSeconds(request.getDurationSeconds());
            record.setCompletedAt(now);
            record.setNextSuggestion(nextSuggestion);
            record.setCreateTime(now);
            record.setUpdateTime(now);
            record.setIsDeleted(0);
            stepRecordMapper.insert(record);
        } else {
            stepRecordMapper.update(null, new LambdaUpdateWrapper<WorkflowStepRecord>()
                    .eq(WorkflowStepRecord::getId, existing.getId())
                    .set(WorkflowStepRecord::getInputContent, request.getInputContent())
                    .set(WorkflowStepRecord::getOutputContent, request.getOutputContent())
                    .set(WorkflowStepRecord::getStatus, STEP_COMPLETED)
                    .set(WorkflowStepRecord::getDurationSeconds, request.getDurationSeconds())
                    .set(WorkflowStepRecord::getCompletedAt, now)
                    .set(WorkflowStepRecord::getNextSuggestion, nextSuggestion)
                    .set(WorkflowStepRecord::getUpdateTime, now));
        }
    }

    private int countCompletedNodes(Long instanceId) {
        return Math.toIntExact(stepRecordMapper.selectCount(new LambdaQueryWrapper<WorkflowStepRecord>()
                .eq(WorkflowStepRecord::getInstanceId, instanceId)
                .eq(WorkflowStepRecord::getStatus, STEP_COMPLETED)));
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

    private WorkflowTemplateNode findNodeInList(List<WorkflowTemplateNode> nodes, Long nodeId) {
        return nodes.stream()
                .filter(node -> Objects.equals(node.getId(), nodeId))
                .findFirst()
                .orElse(null);
    }

    private WorkflowTemplateNode findNode(Long nodeId) {
        return nodeId == null ? null : nodeMapper.selectById(nodeId);
    }

    private WorkflowNextStepVO finishedNextStep() {
        return WorkflowNextStepVO.builder()
                .whetherFinished(true)
                .build();
    }

    private WorkflowInstanceListVO toListVO(WorkflowInstance instance) {
        WorkflowTemplate template = templateMapper.selectById(instance.getTemplateId());
        WorkflowTemplateNode currentNode = findNode(instance.getCurrentNodeId());
        return WorkflowInstanceListVO.builder()
                .id(instance.getId())
                .templateId(instance.getTemplateId())
                .templateName(template == null ? null : template.getName())
                .title(instance.getTitle())
                .currentNodeId(instance.getCurrentNodeId())
                .currentNodeName(currentNode == null ? null : currentNode.getNodeName())
                .status(instance.getStatus())
                .progress(instance.getProgress())
                .startTime(instance.getStartTime())
                .finishTime(instance.getFinishTime())
                .createTime(instance.getCreateTime())
                .build();
    }

    private WorkflowInstanceDetailVO toDetailVO(WorkflowInstance instance) {
        WorkflowTemplate template = templateMapper.selectById(instance.getTemplateId());
        List<WorkflowTemplateNode> nodes = template == null ? Collections.emptyList() : listEnabledNodes(template.getId());
        Map<Long, WorkflowTemplateNode> nodeMap = nodes.stream()
                .collect(Collectors.toMap(WorkflowTemplateNode::getId, Function.identity(), (first, second) -> first));
        WorkflowTemplateNode currentNode = findNode(instance.getCurrentNodeId());
        List<WorkflowStepRecordVO> records = stepRecordMapper.selectList(new LambdaQueryWrapper<WorkflowStepRecord>()
                        .eq(WorkflowStepRecord::getInstanceId, instance.getId())
                        .orderByAsc(WorkflowStepRecord::getCompletedAt)
                        .orderByAsc(WorkflowStepRecord::getId))
                .stream()
                .map(record -> toRecordVO(record, nodeMap.get(record.getNodeId())))
                .toList();

        return WorkflowInstanceDetailVO.builder()
                .id(instance.getId())
                .templateId(instance.getTemplateId())
                .templateName(template == null ? null : template.getName())
                .userId(instance.getUserId())
                .title(instance.getTitle())
                .currentNodeId(instance.getCurrentNodeId())
                .currentNodeName(currentNode == null ? null : currentNode.getNodeName())
                .status(instance.getStatus())
                .progress(instance.getProgress())
                .startTime(instance.getStartTime())
                .finishTime(instance.getFinishTime())
                .nodes(nodes.stream().map(this::toNodeVO).toList())
                .stepRecords(records)
                .createTime(instance.getCreateTime())
                .updateTime(instance.getUpdateTime())
                .build();
    }

    private WorkflowStepRecordVO toRecordVO(WorkflowStepRecord record, WorkflowTemplateNode node) {
        return WorkflowStepRecordVO.builder()
                .id(record.getId())
                .instanceId(record.getInstanceId())
                .nodeId(record.getNodeId())
                .nodeName(node == null ? null : node.getNodeName())
                .inputContent(record.getInputContent())
                .outputContent(record.getOutputContent())
                .status(record.getStatus())
                .durationSeconds(record.getDurationSeconds())
                .startedAt(record.getStartedAt())
                .completedAt(record.getCompletedAt())
                .nextSuggestion(record.getNextSuggestion())
                .build();
    }

    private WorkflowTemplateNodeVO toNodeVO(WorkflowTemplateNode node) {
        return WorkflowTemplateNodeVO.builder()
                .id(node.getId())
                .templateId(node.getTemplateId())
                .stageId(node.getStageId())
                .stepId(node.getStepId())
                .nodeName(node.getNodeName())
                .nodeCode(node.getNodeCode())
                .nodeType(node.getNodeType())
                .inputDesc(node.getInputDesc())
                .outputDesc(node.getOutputDesc())
                .nextTip(node.getNextTip())
                .sortOrder(node.getSortOrder())
                .status(node.getStatus())
                .createTime(node.getCreateTime())
                .updateTime(node.getUpdateTime())
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
