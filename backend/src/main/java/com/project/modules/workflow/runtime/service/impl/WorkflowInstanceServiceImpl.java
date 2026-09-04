package com.project.modules.workflow.runtime.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.common.exception.BusinessException;
import com.project.common.result.PageResult;
import com.project.common.util.PageSupport;
import com.project.modules.profile.service.UserPreferenceContextService;
import com.project.modules.workflow.runtime.dto.WorkflowInstanceCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepCompleteRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepIterationCreateRequest;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import com.project.modules.workflow.runtime.entity.WorkflowStepIteration;
import com.project.modules.workflow.runtime.entity.WorkflowStepRecord;
import com.project.modules.workflow.runtime.entity.WorkflowTemplate;
import com.project.modules.workflow.runtime.entity.WorkflowTemplateNode;
import com.project.modules.workflow.runtime.mapper.WorkflowInstanceMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowStepIterationMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowStepRecordMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateNodeMapper;
import com.project.modules.workflow.runtime.service.WorkflowInstanceService;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceListVO;
import com.project.modules.workflow.runtime.vo.WorkflowNextStepVO;
import com.project.modules.workflow.runtime.vo.WorkflowProgressVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepCompleteVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepIterationVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepRecordVO;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateNodeVO;
import com.project.modules.prompt.service.PromptRevisionService;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.mapper.AiToolMapper;
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
import java.util.stream.Stream;

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
    private final WorkflowStepIterationMapper stepIterationMapper;
    private final AiToolMapper aiToolMapper;
    private final PromptRevisionService promptRevisionService;
    private final UserPreferenceContextService preferenceContextService;

    public WorkflowInstanceServiceImpl(
            WorkflowTemplateMapper templateMapper,
            WorkflowTemplateNodeMapper nodeMapper,
            WorkflowInstanceMapper instanceMapper,
            WorkflowStepRecordMapper stepRecordMapper,
            WorkflowStepIterationMapper stepIterationMapper,
            AiToolMapper aiToolMapper,
            PromptRevisionService promptRevisionService,
            UserPreferenceContextService preferenceContextService
    ) {
        this.templateMapper = templateMapper;
        this.nodeMapper = nodeMapper;
        this.instanceMapper = instanceMapper;
        this.stepRecordMapper = stepRecordMapper;
        this.stepIterationMapper = stepIterationMapper;
        this.aiToolMapper = aiToolMapper;
        this.promptRevisionService = promptRevisionService;
        this.preferenceContextService = preferenceContextService;
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
                .total(result.getTotal())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .pages(result.getPages())
                .build();
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowStepIterationVO createStepIteration(
            Long userId,
            Long instanceId,
            Long nodeId,
            WorkflowStepIterationCreateRequest request
    ) {
        WorkflowInstance instance = getOwnedInstance(userId, instanceId);
        ensureNodeBelongsToTemplate(instance.getTemplateId(), nodeId);
        AiTool tool = getEnabledTool(request.getToolId());
        validatePromptHistory(request);
        if (!hasText(request.getOutputContent()) && !hasText(request.getResultUrl())) {
            throw new BusinessException("Output content or result URL is required");
        }

        WorkflowStepIteration latest = stepIterationMapper.selectOne(
                new LambdaQueryWrapper<WorkflowStepIteration>()
                        .eq(WorkflowStepIteration::getInstanceId, instanceId)
                        .eq(WorkflowStepIteration::getNodeId, nodeId)
                        .orderByDesc(WorkflowStepIteration::getIterationNo)
                        .last("limit 1")
        );

        if (Boolean.TRUE.equals(request.getSelected())) {
            clearSelectedIteration(instanceId, nodeId);
        }

        LocalDateTime now = LocalDateTime.now();
        WorkflowStepIteration iteration = new WorkflowStepIteration();
        iteration.setInstanceId(instanceId);
        iteration.setNodeId(nodeId);
        iteration.setUserId(userId);
        iteration.setIterationNo(latest == null ? 1 : latest.getIterationNo() + 1);
        iteration.setToolId(request.getToolId());
        iteration.setPromptId(request.getPromptId());
        iteration.setPromptRevisionId(request.getPromptRevisionId());
        iteration.setPromptContent(request.getPromptContent());
        iteration.setProfileContextSnapshot(preferenceContextService.buildContextSnapshot(userId));
        iteration.setOutputContent(request.getOutputContent());
        iteration.setResultUrl(request.getResultUrl());
        iteration.setEffectScore(request.getEffectScore());
        iteration.setAccuracyScore(request.getAccuracyScore());
        iteration.setControllabilityScore(request.getControllabilityScore());
        iteration.setUsabilityScore(request.getUsabilityScore());
        iteration.setImprovementNote(request.getImprovementNote());
        iteration.setSelected(Boolean.TRUE.equals(request.getSelected()) ? 1 : 0);
        iteration.setCreateTime(now);
        iteration.setUpdateTime(now);
        iteration.setIsDeleted(0);
        stepIterationMapper.insert(iteration);
        return toIterationVO(iteration, tool);
    }

    @Override
    public List<WorkflowStepIterationVO> listStepIterations(Long userId, Long instanceId, Long nodeId) {
        WorkflowInstance instance = getOwnedInstance(userId, instanceId);
        ensureNodeBelongsToTemplate(instance.getTemplateId(), nodeId);
        List<WorkflowStepIteration> iterations = stepIterationMapper.selectList(
                new LambdaQueryWrapper<WorkflowStepIteration>()
                        .eq(WorkflowStepIteration::getInstanceId, instanceId)
                        .eq(WorkflowStepIteration::getNodeId, nodeId)
                        .orderByAsc(WorkflowStepIteration::getIterationNo)
        );
        Map<Long, AiTool> toolMap = loadToolMap(iterations);
        return iterations.stream()
                .map(iteration -> toIterationVO(iteration, toolMap.get(iteration.getToolId())))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowStepIterationVO selectStepIteration(
            Long userId,
            Long instanceId,
            Long nodeId,
            Long iterationId
    ) {
        WorkflowInstance instance = getOwnedInstance(userId, instanceId);
        ensureNodeBelongsToTemplate(instance.getTemplateId(), nodeId);
        WorkflowStepIteration iteration = stepIterationMapper.selectOne(
                new LambdaQueryWrapper<WorkflowStepIteration>()
                        .eq(WorkflowStepIteration::getId, iterationId)
                        .eq(WorkflowStepIteration::getInstanceId, instanceId)
                        .eq(WorkflowStepIteration::getNodeId, nodeId)
                        .eq(WorkflowStepIteration::getUserId, userId)
                        .last("limit 1")
        );
        if (iteration == null) {
            throw new BusinessException("Workflow step iteration does not exist or you have no permission");
        }

        clearSelectedIteration(instanceId, nodeId);
        LocalDateTime now = LocalDateTime.now();
        stepIterationMapper.update(null, new UpdateWrapper<WorkflowStepIteration>()
                .eq("id", iterationId)
                .set("selected", 1)
                .set("update_time", now));
        iteration.setSelected(1);
        iteration.setUpdateTime(now);
        return toIterationVO(iteration, loadTool(iteration.getToolId()));
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

    private void ensureNodeBelongsToTemplate(Long templateId, Long nodeId) {
        WorkflowTemplateNode node = nodeMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplateNode>()
                .eq(WorkflowTemplateNode::getId, nodeId)
                .eq(WorkflowTemplateNode::getTemplateId, templateId)
                .eq(WorkflowTemplateNode::getStatus, STATUS_ENABLED)
                .last("limit 1"));
        if (node == null) {
            throw new BusinessException("Workflow node does not belong to this template or is disabled");
        }
    }

    private AiTool getEnabledTool(Long toolId) {
        if (toolId == null) {
            return null;
        }
        AiTool tool = aiToolMapper.selectOne(new LambdaQueryWrapper<AiTool>()
                .eq(AiTool::getId, toolId)
                .eq(AiTool::getStatus, STATUS_ENABLED)
                .last("limit 1"));
        if (tool == null) {
            throw new BusinessException("AI tool does not exist or is disabled");
        }
        return tool;
    }

    private AiTool loadTool(Long toolId) {
        return toolId == null ? null : aiToolMapper.selectById(toolId);
    }

    private void validatePromptHistory(WorkflowStepIterationCreateRequest request) {
        boolean hasPromptId = request.getPromptId() != null;
        boolean hasRevisionId = request.getPromptRevisionId() != null;
        if (hasPromptId != hasRevisionId) {
            throw new BusinessException("Prompt ID and prompt revision ID must be provided together");
        }
        if (!hasPromptId) {
            return;
        }
        if (!hasText(request.getPromptContent())) {
            throw new BusinessException("Rendered prompt snapshot is required for a versioned prompt");
        }
        promptRevisionService.requireRevision(request.getPromptId(), request.getPromptRevisionId());
    }

    private void clearSelectedIteration(Long instanceId, Long nodeId) {
        stepIterationMapper.update(null, new UpdateWrapper<WorkflowStepIteration>()
                .eq("instance_id", instanceId)
                .eq("node_id", nodeId)
                .eq("selected", 1)
                .set("selected", 0)
                .set("update_time", LocalDateTime.now()));
    }

    private Map<Long, AiTool> loadToolMap(List<WorkflowStepIteration> iterations) {
        Set<Long> toolIds = iterations.stream()
                .map(WorkflowStepIteration::getToolId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (toolIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return aiToolMapper.selectBatchIds(toolIds).stream()
                .collect(Collectors.toMap(AiTool::getId, Function.identity()));
    }

    private WorkflowStepIterationVO toIterationVO(WorkflowStepIteration iteration, AiTool tool) {
        return WorkflowStepIterationVO.builder()
                .id(iteration.getId())
                .instanceId(iteration.getInstanceId())
                .nodeId(iteration.getNodeId())
                .iterationNo(iteration.getIterationNo())
                .toolId(iteration.getToolId())
                .toolName(tool == null ? null : tool.getName())
                .promptId(iteration.getPromptId())
                .promptRevisionId(iteration.getPromptRevisionId())
                .promptContent(iteration.getPromptContent())
                .profileContextSnapshot(iteration.getProfileContextSnapshot())
                .outputContent(iteration.getOutputContent())
                .resultUrl(iteration.getResultUrl())
                .effectScore(iteration.getEffectScore())
                .accuracyScore(iteration.getAccuracyScore())
                .controllabilityScore(iteration.getControllabilityScore())
                .usabilityScore(iteration.getUsabilityScore())
                .averageScore(calculateAverageScore(iteration))
                .improvementNote(iteration.getImprovementNote())
                .selected(Objects.equals(iteration.getSelected(), 1))
                .createTime(iteration.getCreateTime())
                .updateTime(iteration.getUpdateTime())
                .build();
    }

    private BigDecimal calculateAverageScore(WorkflowStepIteration iteration) {
        List<Integer> scores = Stream.of(
                        iteration.getEffectScore(),
                        iteration.getAccuracyScore(),
                        iteration.getControllabilityScore(),
                        iteration.getUsabilityScore()
                )
                .filter(Objects::nonNull)
                .toList();
        if (scores.isEmpty()) {
            return null;
        }
        int total = scores.stream().mapToInt(Integer::intValue).sum();
        return BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
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

    private List<WorkflowInstanceListVO> toListVOs(List<WorkflowInstance> instances) {
        if (instances.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> templateIds = instances.stream()
                .map(WorkflowInstance::getTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> nodeIds = instances.stream()
                .map(WorkflowInstance::getCurrentNodeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, WorkflowTemplate> templateMap = templateIds.isEmpty()
                ? Collections.emptyMap()
                : templateMapper.selectBatchIds(templateIds).stream()
                .collect(Collectors.toMap(WorkflowTemplate::getId, Function.identity()));
        Map<Long, WorkflowTemplateNode> nodeMap = nodeIds.isEmpty()
                ? Collections.emptyMap()
                : nodeMapper.selectBatchIds(nodeIds).stream()
                .collect(Collectors.toMap(WorkflowTemplateNode::getId, Function.identity()));

        return instances.stream()
                .map(instance -> {
                    WorkflowTemplate template = templateMap.get(instance.getTemplateId());
                    WorkflowTemplateNode currentNode = nodeMap.get(instance.getCurrentNodeId());
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
                })
                .toList();
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
