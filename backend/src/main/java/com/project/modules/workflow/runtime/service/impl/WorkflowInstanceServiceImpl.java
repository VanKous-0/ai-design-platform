package com.project.modules.workflow.runtime.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.common.exception.DomainError;
import com.project.common.exception.DomainException;
import com.project.common.result.PageResult;
import com.project.common.util.PageSupport;
import com.project.modules.workflow.runtime.dto.WorkflowInstanceCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepCompleteRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepIterationCreateRequest;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import com.project.modules.workflow.runtime.entity.WorkflowStepRecord;
import com.project.modules.workflow.runtime.entity.WorkflowTemplate;
import com.project.modules.workflow.runtime.entity.WorkflowTemplateNode;
import com.project.modules.workflow.runtime.mapper.WorkflowInstanceMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowNodeRuntimeMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowStepRecordMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateNodeMapper;
import com.project.modules.workflow.runtime.model.WorkflowInstanceStatus;
import com.project.modules.workflow.runtime.model.WorkflowStepStatus;
import com.project.modules.workflow.runtime.service.WorkflowInstanceService;
import com.project.modules.workflow.runtime.service.WorkflowIterationService;
import com.project.modules.workflow.runtime.service.WorkflowRuntimeAccessService;
import com.project.modules.workflow.runtime.service.WorkflowTransitionService;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceListVO;
import com.project.modules.workflow.runtime.vo.WorkflowNextStepVO;
import com.project.modules.workflow.runtime.vo.WorkflowProgressVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepCompleteVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepIterationVO;
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService {

    private final WorkflowTemplateMapper templateMapper;
    private final WorkflowTemplateNodeMapper nodeMapper;
    private final WorkflowInstanceMapper instanceMapper;
    private final WorkflowStepRecordMapper stepRecordMapper;
    private final WorkflowNodeRuntimeMapper nodeRuntimeMapper;
    private final WorkflowRuntimeAccessService accessService;
    private final WorkflowTransitionService transitionService;
    private final WorkflowIterationService iterationService;

    public WorkflowInstanceServiceImpl(
            WorkflowTemplateMapper templateMapper,
            WorkflowTemplateNodeMapper nodeMapper,
            WorkflowInstanceMapper instanceMapper,
            WorkflowStepRecordMapper stepRecordMapper,
            WorkflowNodeRuntimeMapper nodeRuntimeMapper,
            WorkflowRuntimeAccessService accessService,
            WorkflowTransitionService transitionService,
            WorkflowIterationService iterationService
    ) {
        this.templateMapper = templateMapper;
        this.nodeMapper = nodeMapper;
        this.instanceMapper = instanceMapper;
        this.stepRecordMapper = stepRecordMapper;
        this.nodeRuntimeMapper = nodeRuntimeMapper;
        this.accessService = accessService;
        this.transitionService = transitionService;
        this.iterationService = iterationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowInstanceDetailVO createInstance(Long userId, WorkflowInstanceCreateRequest request) {
        WorkflowTemplate template = accessService.requireEnabledTemplate(request.getTemplateId());
        List<WorkflowTemplateNode> nodes = accessService.listEnabledNodes(template.getId());
        if (nodes.isEmpty()) {
            throw new DomainException(DomainError.WORKFLOW_STATE_CONFLICT,
                    "Workflow template has no enabled nodes");
        }
        LocalDateTime now = LocalDateTime.now();
        WorkflowInstance instance = new WorkflowInstance();
        instance.setTemplateId(template.getId());
        instance.setUserId(userId);
        instance.setTitle(hasText(request.getTitle()) ? request.getTitle().trim() : template.getName());
        instance.setCurrentNodeId(nodes.get(0).getId());
        instance.setStatus(WorkflowInstanceStatus.RUNNING.name());
        instance.setStartTime(now);
        instance.setProgress(BigDecimal.ZERO);
        instance.setLockVersion(0L);
        instance.setCreateTime(now);
        instance.setUpdateTime(now);
        instance.setIsDeleted(0);
        instanceMapper.insert(instance);
        nodes.forEach(node -> nodeRuntimeMapper.insertIfAbsent(instance.getId(), node.getId()));
        return toDetailVO(instance);
    }

    @Override
    public List<WorkflowInstanceListVO> listMyInstances(Long userId) {
        return toListVOs(instanceMapper.selectList(new LambdaQueryWrapper<WorkflowInstance>()
                .eq(WorkflowInstance::getUserId, userId)
                .orderByDesc(WorkflowInstance::getCreateTime)
                .orderByDesc(WorkflowInstance::getId)));
    }

    @Override
    public PageResult<WorkflowInstanceListVO> pageMyInstances(Long userId, Long pageNum, Long pageSize) {
        Page<WorkflowInstance> page = PageSupport.page(pageNum, pageSize);
        Page<WorkflowInstance> result = instanceMapper.selectPage(page, new LambdaQueryWrapper<WorkflowInstance>()
                .eq(WorkflowInstance::getUserId, userId)
                .orderByDesc(WorkflowInstance::getCreateTime)
                .orderByDesc(WorkflowInstance::getId));
        return PageResult.<WorkflowInstanceListVO>builder()
                .records(toListVOs(result.getRecords()))
                .total(result.getTotal()).pageNum(result.getCurrent()).pageSize(result.getSize())
                .pages(result.getPages()).build();
    }

    @Override
    public WorkflowInstanceDetailVO getMyInstance(Long userId, Long instanceId) {
        return toDetailVO(accessService.requireOwnedInstance(userId, instanceId));
    }

    @Override
    public WorkflowProgressVO getProgress(Long userId, Long instanceId) {
        WorkflowInstance instance = accessService.requireOwnedInstance(userId, instanceId);
        List<WorkflowTemplateNode> nodes = accessService.listEnabledNodes(instance.getTemplateId());
        int completedCount = countCompletedNodes(instanceId);
        WorkflowTemplateNode currentNode = findNode(instance.getCurrentNodeId());
        return WorkflowProgressVO.builder()
                .instanceId(instanceId).totalNodeCount(nodes.size()).completedNodeCount(completedCount)
                .progress(calculateProgress(completedCount, nodes.size())).status(instance.getStatus())
                .currentNodeId(instance.getCurrentNodeId())
                .currentNodeName(currentNode == null ? null : currentNode.getNodeName()).build();
    }

    @Override
    public WorkflowNextStepVO getNextStep(Long userId, Long instanceId) {
        WorkflowInstance instance = accessService.requireOwnedInstance(userId, instanceId);
        if (WorkflowInstanceStatus.FINISHED.name().equals(instance.getStatus())
                || instance.getCurrentNodeId() == null) {
            return finishedNextStep();
        }
        WorkflowTemplateNode currentNode = findNode(instance.getCurrentNodeId());
        return WorkflowNextStepVO.builder()
                .nextNodeId(instance.getCurrentNodeId())
                .nextNodeName(currentNode == null ? null : currentNode.getNodeName())
                .nextTip(currentNode == null ? null : currentNode.getNextTip())
                .whetherFinished(false).build();
    }

    @Override
    public WorkflowStepCompleteVO completeStep(
            Long userId, Long instanceId, Long nodeId, WorkflowStepCompleteRequest request, String idempotencyKey
    ) {
        return transitionService.completeStep(userId, instanceId, nodeId, request, idempotencyKey);
    }

    @Override
    public WorkflowStepIterationVO createStepIteration(
            Long userId,
            Long instanceId,
            Long nodeId,
            WorkflowStepIterationCreateRequest request,
            String idempotencyKey
    ) {
        return iterationService.create(userId, instanceId, nodeId, request, idempotencyKey);
    }

    @Override
    public List<WorkflowStepIterationVO> listStepIterations(Long userId, Long instanceId, Long nodeId) {
        return iterationService.list(userId, instanceId, nodeId);
    }

    @Override
    public WorkflowStepIterationVO selectStepIteration(
            Long userId, Long instanceId, Long nodeId, Long iterationId
    ) {
        return iterationService.select(userId, instanceId, nodeId, iterationId);
    }

    private int countCompletedNodes(Long instanceId) {
        return Math.toIntExact(stepRecordMapper.selectCount(new LambdaQueryWrapper<WorkflowStepRecord>()
                .eq(WorkflowStepRecord::getInstanceId, instanceId)
                .eq(WorkflowStepRecord::getStatus, WorkflowStepStatus.COMPLETED.name())));
    }

    private BigDecimal calculateProgress(int completedCount, int totalCount) {
        if (totalCount <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(completedCount).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
    }

    private WorkflowTemplateNode findNode(Long nodeId) {
        return nodeId == null ? null : nodeMapper.selectById(nodeId);
    }

    private WorkflowNextStepVO finishedNextStep() {
        return WorkflowNextStepVO.builder().whetherFinished(true).build();
    }

    private List<WorkflowInstanceListVO> toListVOs(List<WorkflowInstance> instances) {
        if (instances.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> templateIds = instances.stream().map(WorkflowInstance::getTemplateId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> nodeIds = instances.stream().map(WorkflowInstance::getCurrentNodeId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, WorkflowTemplate> templateMap = templateIds.isEmpty() ? Collections.emptyMap()
                : templateMapper.selectBatchIds(templateIds).stream()
                        .collect(Collectors.toMap(WorkflowTemplate::getId, Function.identity()));
        Map<Long, WorkflowTemplateNode> nodeMap = nodeIds.isEmpty() ? Collections.emptyMap()
                : nodeMapper.selectBatchIds(nodeIds).stream()
                        .collect(Collectors.toMap(WorkflowTemplateNode::getId, Function.identity()));
        return instances.stream().map(instance -> {
            WorkflowTemplate template = templateMap.get(instance.getTemplateId());
            WorkflowTemplateNode node = nodeMap.get(instance.getCurrentNodeId());
            return WorkflowInstanceListVO.builder()
                    .id(instance.getId()).templateId(instance.getTemplateId())
                    .templateName(template == null ? null : template.getName()).title(instance.getTitle())
                    .currentNodeId(instance.getCurrentNodeId())
                    .currentNodeName(node == null ? null : node.getNodeName())
                    .status(instance.getStatus()).progress(instance.getProgress())
                    .startTime(instance.getStartTime()).finishTime(instance.getFinishTime())
                    .createTime(instance.getCreateTime()).build();
        }).toList();
    }

    private WorkflowInstanceDetailVO toDetailVO(WorkflowInstance instance) {
        WorkflowTemplate template = templateMapper.selectById(instance.getTemplateId());
        List<WorkflowTemplateNode> nodes = template == null ? Collections.emptyList()
                : accessService.listEnabledNodes(template.getId());
        Map<Long, WorkflowTemplateNode> nodeMap = nodes.stream().collect(Collectors.toMap(
                WorkflowTemplateNode::getId, Function.identity(), (first, second) -> first
        ));
        WorkflowTemplateNode currentNode = findNode(instance.getCurrentNodeId());
        List<WorkflowStepRecordVO> records = stepRecordMapper.selectList(
                        new LambdaQueryWrapper<WorkflowStepRecord>()
                                .eq(WorkflowStepRecord::getInstanceId, instance.getId())
                                .orderByAsc(WorkflowStepRecord::getCompletedAt)
                                .orderByAsc(WorkflowStepRecord::getId))
                .stream().map(record -> toRecordVO(record, nodeMap.get(record.getNodeId()))).toList();
        return WorkflowInstanceDetailVO.builder()
                .id(instance.getId()).templateId(instance.getTemplateId())
                .templateName(template == null ? null : template.getName()).userId(instance.getUserId())
                .title(instance.getTitle()).currentNodeId(instance.getCurrentNodeId())
                .currentNodeName(currentNode == null ? null : currentNode.getNodeName())
                .status(instance.getStatus()).progress(instance.getProgress())
                .startTime(instance.getStartTime()).finishTime(instance.getFinishTime())
                .nodes(nodes.stream().map(this::toNodeVO).toList()).stepRecords(records)
                .createTime(instance.getCreateTime()).updateTime(instance.getUpdateTime()).build();
    }

    private WorkflowStepRecordVO toRecordVO(WorkflowStepRecord record, WorkflowTemplateNode node) {
        return WorkflowStepRecordVO.builder()
                .id(record.getId()).instanceId(record.getInstanceId()).nodeId(record.getNodeId())
                .nodeName(node == null ? null : node.getNodeName())
                .inputContent(record.getInputContent()).outputContent(record.getOutputContent())
                .status(record.getStatus()).durationSeconds(record.getDurationSeconds())
                .startedAt(record.getStartedAt()).completedAt(record.getCompletedAt())
                .nextSuggestion(record.getNextSuggestion()).build();
    }

    private WorkflowTemplateNodeVO toNodeVO(WorkflowTemplateNode node) {
        return WorkflowTemplateNodeVO.builder()
                .id(node.getId()).templateId(node.getTemplateId()).stageId(node.getStageId())
                .stepId(node.getStepId()).nodeName(node.getNodeName()).nodeCode(node.getNodeCode())
                .nodeType(node.getNodeType()).inputDesc(node.getInputDesc()).outputDesc(node.getOutputDesc())
                .nextTip(node.getNextTip()).sortOrder(node.getSortOrder()).status(node.getStatus())
                .createTime(node.getCreateTime()).updateTime(node.getUpdateTime()).build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
