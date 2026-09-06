package com.project.modules.workflow.runtime.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.project.common.exception.DomainError;
import com.project.common.exception.DomainException;
import com.project.common.idempotency.IdempotencySupport;
import com.project.modules.profile.service.UserPreferenceContextService;
import com.project.modules.prompt.service.PromptRevisionService;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.mapper.AiToolMapper;
import com.project.modules.workflow.runtime.dto.WorkflowStepIterationCreateRequest;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import com.project.modules.workflow.runtime.entity.WorkflowNodeRuntime;
import com.project.modules.workflow.runtime.entity.WorkflowStepIteration;
import com.project.modules.workflow.runtime.mapper.WorkflowNodeRuntimeMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowStepIterationMapper;
import com.project.modules.workflow.runtime.service.WorkflowIterationService;
import com.project.modules.workflow.runtime.service.WorkflowRuntimeAccessService;
import com.project.modules.workflow.runtime.vo.WorkflowStepIterationVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WorkflowIterationServiceImpl implements WorkflowIterationService {

    private final WorkflowRuntimeAccessService accessService;
    private final WorkflowStepIterationMapper iterationMapper;
    private final WorkflowNodeRuntimeMapper runtimeMapper;
    private final AiToolMapper aiToolMapper;
    private final PromptRevisionService promptRevisionService;
    private final UserPreferenceContextService preferenceContextService;
    private final IdempotencySupport idempotencySupport;

    public WorkflowIterationServiceImpl(
            WorkflowRuntimeAccessService accessService,
            WorkflowStepIterationMapper iterationMapper,
            WorkflowNodeRuntimeMapper runtimeMapper,
            AiToolMapper aiToolMapper,
            PromptRevisionService promptRevisionService,
            UserPreferenceContextService preferenceContextService,
            IdempotencySupport idempotencySupport
    ) {
        this.accessService = accessService;
        this.iterationMapper = iterationMapper;
        this.runtimeMapper = runtimeMapper;
        this.aiToolMapper = aiToolMapper;
        this.promptRevisionService = promptRevisionService;
        this.preferenceContextService = preferenceContextService;
        this.idempotencySupport = idempotencySupport;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowStepIterationVO create(
            Long userId,
            Long instanceId,
            Long nodeId,
            WorkflowStepIterationCreateRequest request,
            String idempotencyKey
    ) {
        WorkflowInstance instance = accessService.requireOwnedInstance(userId, instanceId);
        accessService.requireEnabledNode(instance.getTemplateId(), nodeId);
        String requestId = idempotencySupport.normalizeKey(idempotencyKey);
        String requestHash = requestId == null ? null : creationFingerprint(nodeId, request);
        WorkflowStepIteration replay = findReplay(instanceId, requestId, requestHash);
        if (replay != null) {
            return toVO(replay, loadTool(replay.getToolId()), canonicalSelectedId(instanceId, nodeId));
        }

        AiTool tool = accessService.requireEnabledTool(request.getToolId());
        validatePromptHistory(request);
        if (!hasText(request.getOutputContent()) && !hasText(request.getResultUrl())) {
            throw new DomainException(DomainError.VALIDATION_ERROR,
                    "Output content or result URL is required");
        }
        String profileSnapshot = preferenceContextService.buildContextSnapshot(userId);

        WorkflowNodeRuntime runtime = runtimeMapper.selectForUpdate(instanceId, nodeId);
        if (runtime == null) {
            throw new DomainException(DomainError.WORKFLOW_ITERATION_CONFLICT,
                    "Workflow node runtime could not be initialized");
        }
        replay = findReplay(instanceId, requestId, requestHash);
        if (replay != null) {
            return toVO(replay, loadTool(replay.getToolId()), runtime.getSelectedIterationId());
        }

        int iterationNo = runtime.getNextIterationNo();
        if (runtimeMapper.advanceIterationCounter(instanceId, nodeId, iterationNo) != 1) {
            throw new DomainException(DomainError.WORKFLOW_ITERATION_CONFLICT);
        }
        LocalDateTime now = LocalDateTime.now();
        WorkflowStepIteration iteration = new WorkflowStepIteration();
        iteration.setInstanceId(instanceId);
        iteration.setNodeId(nodeId);
        iteration.setUserId(userId);
        iteration.setIterationNo(iterationNo);
        iteration.setToolId(request.getToolId());
        iteration.setPromptId(request.getPromptId());
        iteration.setPromptRevisionId(request.getPromptRevisionId());
        iteration.setPromptContent(request.getPromptContent());
        iteration.setProfileContextSnapshot(profileSnapshot);
        iteration.setOutputContent(request.getOutputContent());
        iteration.setResultUrl(request.getResultUrl());
        iteration.setEffectScore(request.getEffectScore());
        iteration.setAccuracyScore(request.getAccuracyScore());
        iteration.setControllabilityScore(request.getControllabilityScore());
        iteration.setUsabilityScore(request.getUsabilityScore());
        iteration.setImprovementNote(request.getImprovementNote());
        iteration.setSelected(0);
        iteration.setCreationRequestId(requestId);
        iteration.setCreationRequestHash(requestHash);
        iteration.setCreateTime(now);
        iteration.setUpdateTime(now);
        iteration.setIsDeleted(0);
        try {
            iterationMapper.insert(iteration);
        } catch (DuplicateKeyException ex) {
            throw new DomainException(DomainError.WORKFLOW_ITERATION_CONFLICT,
                    "Iteration number or idempotency key conflicts with existing state");
        }

        Long selectedId = runtime.getSelectedIterationId();
        if (Boolean.TRUE.equals(request.getSelected())) {
            chooseCanonical(instanceId, nodeId, iteration.getId(), now);
            selectedId = iteration.getId();
        }
        return toVO(iteration, tool, selectedId);
    }

    @Override
    public List<WorkflowStepIterationVO> list(Long userId, Long instanceId, Long nodeId) {
        WorkflowInstance instance = accessService.requireOwnedInstance(userId, instanceId);
        accessService.requireEnabledNode(instance.getTemplateId(), nodeId);
        List<WorkflowStepIteration> iterations = iterationMapper.selectList(
                new LambdaQueryWrapper<WorkflowStepIteration>()
                        .eq(WorkflowStepIteration::getInstanceId, instanceId)
                        .eq(WorkflowStepIteration::getNodeId, nodeId)
                        .orderByAsc(WorkflowStepIteration::getIterationNo)
        );
        Map<Long, AiTool> tools = loadToolMap(iterations);
        Long selectedId = canonicalSelectedId(instanceId, nodeId);
        return iterations.stream().map(item -> toVO(item, tools.get(item.getToolId()), selectedId)).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowStepIterationVO select(Long userId, Long instanceId, Long nodeId, Long iterationId) {
        WorkflowInstance instance = accessService.requireOwnedInstance(userId, instanceId);
        accessService.requireEnabledNode(instance.getTemplateId(), nodeId);
        WorkflowStepIteration iteration = iterationMapper.selectOne(
                new LambdaQueryWrapper<WorkflowStepIteration>()
                        .eq(WorkflowStepIteration::getId, iterationId)
                        .eq(WorkflowStepIteration::getInstanceId, instanceId)
                        .eq(WorkflowStepIteration::getNodeId, nodeId)
                        .eq(WorkflowStepIteration::getUserId, userId)
                        .last("limit 1")
        );
        if (iteration == null) {
            throw new DomainException(DomainError.WORKFLOW_NOT_FOUND,
                    "Workflow step iteration does not exist");
        }
        if (runtimeMapper.selectForUpdate(instanceId, nodeId) == null) {
            throw new DomainException(DomainError.WORKFLOW_ITERATION_CONFLICT);
        }
        LocalDateTime now = LocalDateTime.now();
        chooseCanonical(instanceId, nodeId, iterationId, now);
        iteration.setSelected(1);
        iteration.setUpdateTime(now);
        return toVO(iteration, loadTool(iteration.getToolId()), iterationId);
    }

    private void chooseCanonical(Long instanceId, Long nodeId, Long iterationId, LocalDateTime now) {
        iterationMapper.update(null, new UpdateWrapper<WorkflowStepIteration>()
                .eq("instance_id", instanceId)
                .eq("node_id", nodeId)
                .eq("selected", 1)
                .set("selected", 0)
                .set("update_time", now));
        int selected = iterationMapper.update(null, new UpdateWrapper<WorkflowStepIteration>()
                .eq("id", iterationId)
                .eq("instance_id", instanceId)
                .eq("node_id", nodeId)
                .set("selected", 1)
                .set("update_time", now));
        if (selected != 1 || runtimeMapper.setSelectedIteration(instanceId, nodeId, iterationId) != 1) {
            throw new DomainException(DomainError.WORKFLOW_ITERATION_CONFLICT);
        }
    }

    private WorkflowStepIteration findReplay(Long instanceId, String requestId, String requestHash) {
        if (requestId == null) {
            return null;
        }
        WorkflowStepIteration existing = iterationMapper.selectByCreationRequest(instanceId, requestId);
        if (existing != null) {
            idempotencySupport.requireSameRequest(existing.getCreationRequestHash(), requestHash);
        }
        return existing;
    }

    private String creationFingerprint(Long nodeId, WorkflowStepIterationCreateRequest request) {
        Map<String, Object> shape = new LinkedHashMap<>();
        shape.put("nodeId", nodeId);
        shape.put("toolId", request.getToolId());
        shape.put("promptId", request.getPromptId());
        shape.put("promptRevisionId", request.getPromptRevisionId());
        shape.put("promptContent", request.getPromptContent());
        shape.put("outputContent", request.getOutputContent());
        shape.put("resultUrl", request.getResultUrl());
        shape.put("effectScore", request.getEffectScore());
        shape.put("accuracyScore", request.getAccuracyScore());
        shape.put("controllabilityScore", request.getControllabilityScore());
        shape.put("usabilityScore", request.getUsabilityScore());
        shape.put("improvementNote", request.getImprovementNote());
        shape.put("selected", Boolean.TRUE.equals(request.getSelected()));
        return idempotencySupport.fingerprint(shape);
    }

    private void validatePromptHistory(WorkflowStepIterationCreateRequest request) {
        boolean hasPromptId = request.getPromptId() != null;
        boolean hasRevisionId = request.getPromptRevisionId() != null;
        if (hasPromptId != hasRevisionId) {
            throw new DomainException(DomainError.PROMPT_REVISION_MISMATCH,
                    "Prompt ID and prompt revision ID must be provided together");
        }
        if (!hasPromptId) {
            return;
        }
        if (!hasText(request.getPromptContent())) {
            throw new DomainException(DomainError.VALIDATION_ERROR,
                    "Rendered prompt snapshot is required for a versioned prompt");
        }
        promptRevisionService.requireRevision(request.getPromptId(), request.getPromptRevisionId());
    }

    private Long canonicalSelectedId(Long instanceId, Long nodeId) {
        WorkflowNodeRuntime runtime = runtimeMapper.selectRuntime(instanceId, nodeId);
        return runtime == null ? null : runtime.getSelectedIterationId();
    }

    private Map<Long, AiTool> loadToolMap(List<WorkflowStepIteration> iterations) {
        Set<Long> ids = iterations.stream().map(WorkflowStepIteration::getToolId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return aiToolMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(AiTool::getId, Function.identity()));
    }

    private AiTool loadTool(Long toolId) {
        return toolId == null ? null : aiToolMapper.selectById(toolId);
    }

    private WorkflowStepIterationVO toVO(WorkflowStepIteration item, AiTool tool, Long selectedId) {
        return WorkflowStepIterationVO.builder()
                .id(item.getId()).instanceId(item.getInstanceId()).nodeId(item.getNodeId())
                .iterationNo(item.getIterationNo()).toolId(item.getToolId())
                .toolName(tool == null ? null : tool.getName())
                .promptId(item.getPromptId()).promptRevisionId(item.getPromptRevisionId())
                .promptContent(item.getPromptContent()).profileContextSnapshot(item.getProfileContextSnapshot())
                .outputContent(item.getOutputContent()).resultUrl(item.getResultUrl())
                .effectScore(item.getEffectScore()).accuracyScore(item.getAccuracyScore())
                .controllabilityScore(item.getControllabilityScore()).usabilityScore(item.getUsabilityScore())
                .averageScore(calculateAverageScore(item)).improvementNote(item.getImprovementNote())
                .selected(Objects.equals(item.getId(), selectedId))
                .createTime(item.getCreateTime()).updateTime(item.getUpdateTime()).build();
    }

    private BigDecimal calculateAverageScore(WorkflowStepIteration iteration) {
        List<Integer> scores = Stream.of(iteration.getEffectScore(), iteration.getAccuracyScore(),
                        iteration.getControllabilityScore(), iteration.getUsabilityScore())
                .filter(Objects::nonNull).toList();
        if (scores.isEmpty()) {
            return null;
        }
        return BigDecimal.valueOf(scores.stream().mapToInt(Integer::intValue).sum())
                .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
