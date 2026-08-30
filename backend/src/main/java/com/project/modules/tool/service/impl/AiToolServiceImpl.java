package com.project.modules.tool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.common.exception.BusinessException;
import com.project.common.result.PageResult;
import com.project.common.util.PageSupport;
import com.project.modules.tool.dto.AiToolCreateRequest;
import com.project.modules.tool.dto.AiToolUpdateRequest;
import com.project.modules.tool.dto.ToolEvaluationSaveRequest;
import com.project.modules.tool.dto.ToolStageSetRequest;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.entity.AiToolEvaluation;
import com.project.modules.tool.entity.AiToolStageRel;
import com.project.modules.tool.entity.EvaluationDimension;
import com.project.modules.tool.mapper.AiToolEvaluationMapper;
import com.project.modules.tool.mapper.AiToolMapper;
import com.project.modules.tool.mapper.AiToolStageRelMapper;
import com.project.modules.tool.mapper.EvaluationDimensionMapper;
import com.project.modules.tool.service.AiToolService;
import com.project.modules.tool.vo.AiToolEvaluationVO;
import com.project.modules.tool.vo.AiToolVO;
import com.project.modules.tool.vo.ToolStageVO;
import com.project.modules.workflow.entity.WorkflowStage;
import com.project.modules.workflow.mapper.WorkflowStageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AiToolServiceImpl implements AiToolService {

    private static final int STATUS_ENABLED = 1;
    private static final int DEFAULT_STATUS = 1;
    private static final String DEFAULT_TOOL_DATA_STATUS = "MANUAL";
    private static final String DEFAULT_EVALUATION_DATA_STATUS = "MANUAL";

    private final AiToolMapper aiToolMapper;
    private final AiToolStageRelMapper aiToolStageRelMapper;
    private final EvaluationDimensionMapper evaluationDimensionMapper;
    private final AiToolEvaluationMapper aiToolEvaluationMapper;
    private final WorkflowStageMapper workflowStageMapper;

    public AiToolServiceImpl(
            AiToolMapper aiToolMapper,
            AiToolStageRelMapper aiToolStageRelMapper,
            EvaluationDimensionMapper evaluationDimensionMapper,
            AiToolEvaluationMapper aiToolEvaluationMapper,
            WorkflowStageMapper workflowStageMapper
    ) {
        this.aiToolMapper = aiToolMapper;
        this.aiToolStageRelMapper = aiToolStageRelMapper;
        this.evaluationDimensionMapper = evaluationDimensionMapper;
        this.aiToolEvaluationMapper = aiToolEvaluationMapper;
        this.workflowStageMapper = workflowStageMapper;
    }

    @Override
    public List<AiToolVO> listTools(Long stageId, String keyword) {
        List<Long> filteredToolIds = findFilteredToolIds(stageId);
        if (filteredToolIds != null && filteredToolIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<AiTool> tools = aiToolMapper.selectList(buildToolQuery(filteredToolIds, keyword));
        return toToolVOList(tools);
    }

    @Override
    public PageResult<AiToolVO> pageTools(
            Long stageId,
            String keyword,
            Long pageNum,
            Long pageSize
    ) {
        List<Long> filteredToolIds = findFilteredToolIds(stageId);
        Page<AiTool> page = PageSupport.page(pageNum, pageSize);
        if (filteredToolIds != null && filteredToolIds.isEmpty()) {
            return PageResult.<AiToolVO>builder()
                    .records(Collections.emptyList())
                    .total(0)
                    .pageNum(page.getCurrent())
                    .pageSize(page.getSize())
                    .pages(0)
                    .build();
        }
        Page<AiTool> result = aiToolMapper.selectPage(page, buildToolQuery(filteredToolIds, keyword));
        return PageResult.<AiToolVO>builder()
                .records(toToolVOList(result.getRecords()))
                .total(result.getTotal())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .pages(result.getPages())
                .build();
    }

    private List<Long> findFilteredToolIds(Long stageId) {
        List<Long> filteredToolIds = null;
        if (stageId != null) {
            ensureStageExists(stageId);
            filteredToolIds = aiToolStageRelMapper.selectList(new LambdaQueryWrapper<AiToolStageRel>()
                            .eq(AiToolStageRel::getStageId, stageId))
                    .stream()
                    .map(AiToolStageRel::getToolId)
                    .distinct()
                    .toList();
        }
        return filteredToolIds;
    }

    private LambdaQueryWrapper<AiTool> buildToolQuery(List<Long> filteredToolIds, String keyword) {
        LambdaQueryWrapper<AiTool> queryWrapper = new LambdaQueryWrapper<AiTool>()
                .eq(AiTool::getStatus, STATUS_ENABLED)
                .orderByAsc(AiTool::getId);
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(AiTool::getName, keyword.trim());
        }
        if (filteredToolIds != null) {
            queryWrapper.in(AiTool::getId, filteredToolIds);
        }
        return queryWrapper;
    }

    @Override
    public AiToolVO getToolDetail(Long id) {
        AiTool tool = getEnabledToolEntity(id);
        return toToolVO(tool, loadStageVosByToolIds(List.of(tool.getId())));
    }

    @Override
    public List<AiToolEvaluationVO> listEvaluations(Long toolId) {
        getEnabledToolEntity(toolId);
        return listEvaluationsInternal(toolId);
    }

    @Override
    public List<AiToolVO> recommendTools(Long stageId) {
        if (stageId == null) {
            throw new BusinessException("阶段ID不能为空");
        }
        return listTools(stageId, null);
    }

    @Override
    public AiToolVO createTool(AiToolCreateRequest request) {
        ensureToolCodeUnique(request.getCode(), null);

        LocalDateTime now = LocalDateTime.now();
        AiTool tool = new AiTool();
        tool.setName(request.getName());
        tool.setCode(request.getCode());
        tool.setOfficialUrl(request.getOfficialUrl());
        tool.setLogoUrl(request.getLogoUrl());
        tool.setDescription(request.getDescription());
        tool.setPriceDesc(request.getPriceDesc());
        tool.setVersionDesc(request.getVersionDesc());
        tool.setDataStatus(normalizeDataStatus(request.getDataStatus(), DEFAULT_TOOL_DATA_STATUS));
        tool.setSourceDesc(request.getSourceDesc());
        tool.setStatus(defaultIfNull(request.getStatus(), DEFAULT_STATUS));
        tool.setCreateTime(now);
        tool.setUpdateTime(now);
        tool.setIsDeleted(0);
        aiToolMapper.insert(tool);
        return toToolVO(tool, Collections.emptyMap());
    }

    @Override
    public AiToolVO updateTool(Long id, AiToolUpdateRequest request) {
        AiTool tool = getToolEntity(id);
        ensureToolCodeUnique(request.getCode(), id);

        tool.setName(request.getName());
        tool.setCode(request.getCode());
        tool.setOfficialUrl(request.getOfficialUrl());
        tool.setLogoUrl(request.getLogoUrl());
        tool.setDescription(request.getDescription());
        tool.setPriceDesc(request.getPriceDesc());
        tool.setVersionDesc(request.getVersionDesc());
        tool.setDataStatus(normalizeDataStatus(request.getDataStatus(), DEFAULT_TOOL_DATA_STATUS));
        tool.setSourceDesc(request.getSourceDesc());
        tool.setStatus(defaultIfNull(request.getStatus(), DEFAULT_STATUS));
        tool.setUpdateTime(LocalDateTime.now());
        aiToolMapper.updateById(tool);
        return toToolVO(tool, loadStageVosByToolIds(List.of(tool.getId())));
    }

    @Override
    public void deleteTool(Long id) {
        getToolEntity(id);
        aiToolMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> setToolStages(Long toolId, ToolStageSetRequest request) {
        getToolEntity(toolId);
        List<Long> stageIds = normalizeIds(request.getStageIds(), "阶段ID不能为空");
        ensureStagesExist(stageIds);

        aiToolStageRelMapper.delete(new LambdaQueryWrapper<AiToolStageRel>()
                .eq(AiToolStageRel::getToolId, toolId));

        LocalDateTime now = LocalDateTime.now();
        for (Long stageId : stageIds) {
            AiToolStageRel rel = new AiToolStageRel();
            rel.setToolId(toolId);
            rel.setStageId(stageId);
            rel.setCreateTime(now);
            aiToolStageRelMapper.insert(rel);
        }
        return stageIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AiToolEvaluationVO> saveToolEvaluations(Long toolId, ToolEvaluationSaveRequest request) {
        getToolEntity(toolId);
        Map<Long, ToolEvaluationSaveRequest.EvaluationItem> itemMap = request.getEvaluations()
                .stream()
                .collect(Collectors.toMap(
                        ToolEvaluationSaveRequest.EvaluationItem::getDimensionId,
                        Function.identity(),
                        (first, second) -> second,
                        LinkedHashMap::new
                ));
        ensureDimensionsExist(new ArrayList<>(itemMap.keySet()));

        LocalDateTime now = LocalDateTime.now();
        for (ToolEvaluationSaveRequest.EvaluationItem item : itemMap.values()) {
            BigDecimal score = item.getScore();
            AiToolEvaluation existing = aiToolEvaluationMapper.selectOne(new LambdaQueryWrapper<AiToolEvaluation>()
                    .eq(AiToolEvaluation::getToolId, toolId)
                    .eq(AiToolEvaluation::getDimensionId, item.getDimensionId())
                    .last("limit 1"));
            if (existing == null) {
                AiToolEvaluation evaluation = new AiToolEvaluation();
                evaluation.setToolId(toolId);
                evaluation.setDimensionId(item.getDimensionId());
                evaluation.setScore(score);
                evaluation.setComment(item.getComment());
                evaluation.setDataStatus(normalizeDataStatus(
                        item.getDataStatus(),
                        DEFAULT_EVALUATION_DATA_STATUS
                ));
                evaluation.setSourceDesc(item.getSourceDesc());
                evaluation.setCreateTime(now);
                evaluation.setUpdateTime(now);
                aiToolEvaluationMapper.insert(evaluation);
            } else {
                existing.setScore(score);
                existing.setComment(item.getComment());
                existing.setDataStatus(normalizeDataStatus(
                        item.getDataStatus(),
                        DEFAULT_EVALUATION_DATA_STATUS
                ));
                existing.setSourceDesc(item.getSourceDesc());
                existing.setUpdateTime(now);
                aiToolEvaluationMapper.updateById(existing);
            }
        }

        return listEvaluationsInternal(toolId);
    }

    private List<AiToolEvaluationVO> listEvaluationsInternal(Long toolId) {
        List<EvaluationDimension> dimensions = evaluationDimensionMapper.selectList(new LambdaQueryWrapper<EvaluationDimension>()
                .eq(EvaluationDimension::getStatus, STATUS_ENABLED)
                .orderByAsc(EvaluationDimension::getSortOrder)
                .orderByAsc(EvaluationDimension::getId));
        if (dimensions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, AiToolEvaluation> evaluationMap = aiToolEvaluationMapper.selectList(new LambdaQueryWrapper<AiToolEvaluation>()
                        .eq(AiToolEvaluation::getToolId, toolId)
                        .ne(AiToolEvaluation::getDataStatus, "DEMO"))
                .stream()
                .collect(Collectors.toMap(AiToolEvaluation::getDimensionId, Function.identity(), (first, second) -> second));

        return dimensions.stream()
                .filter(dimension -> evaluationMap.containsKey(dimension.getId()))
                .map(dimension -> {
                    AiToolEvaluation evaluation = evaluationMap.get(dimension.getId());
                    return AiToolEvaluationVO.builder()
                            .dimensionId(dimension.getId())
                            .dimensionName(dimension.getName())
                            .dimensionCode(dimension.getCode())
                            .dimensionDescription(dimension.getDescription())
                            .weightPercent(dimension.getWeightPercent())
                            .sortOrder(dimension.getSortOrder())
                            .score(evaluation.getScore())
                            .comment(evaluation.getComment())
                            .dataStatus(evaluation.getDataStatus())
                            .sourceDesc(evaluation.getSourceDesc())
                            .build();
                })
                .toList();
    }

    private List<AiToolVO> toToolVOList(List<AiTool> tools) {
        if (tools.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<ToolStageVO>> stageMap = loadStageVosByToolIds(tools.stream().map(AiTool::getId).toList());
        return tools.stream()
                .map(tool -> toToolVO(tool, stageMap))
                .toList();
    }

    private Map<Long, List<ToolStageVO>> loadStageVosByToolIds(List<Long> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<AiToolStageRel> rels = aiToolStageRelMapper.selectList(new LambdaQueryWrapper<AiToolStageRel>()
                .in(AiToolStageRel::getToolId, toolIds));
        if (rels.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> stageIds = rels.stream()
                .map(AiToolStageRel::getStageId)
                .collect(Collectors.toSet());
        Map<Long, WorkflowStage> stageMap = workflowStageMapper.selectBatchIds(stageIds)
                .stream()
                .filter(stage -> Objects.equals(stage.getStatus(), STATUS_ENABLED))
                .collect(Collectors.toMap(WorkflowStage::getId, Function.identity()));

        Map<Long, List<ToolStageVO>> result = new HashMap<>();
        for (AiToolStageRel rel : rels) {
            WorkflowStage stage = stageMap.get(rel.getStageId());
            if (stage == null) {
                continue;
            }
            result.computeIfAbsent(rel.getToolId(), key -> new ArrayList<>())
                    .add(ToolStageVO.builder()
                            .id(stage.getId())
                            .name(stage.getName())
                            .code(stage.getCode())
                            .build());
        }
        return result;
    }

    private AiToolVO toToolVO(AiTool tool, Map<Long, List<ToolStageVO>> stageMap) {
        return AiToolVO.builder()
                .id(tool.getId())
                .name(tool.getName())
                .code(tool.getCode())
                .officialUrl(tool.getOfficialUrl())
                .logoUrl(tool.getLogoUrl())
                .description(tool.getDescription())
                .priceDesc(tool.getPriceDesc())
                .versionDesc(tool.getVersionDesc())
                .dataStatus(tool.getDataStatus())
                .sourceDesc(tool.getSourceDesc())
                .status(tool.getStatus())
                .stages(stageMap.getOrDefault(tool.getId(), Collections.emptyList()))
                .createTime(tool.getCreateTime())
                .updateTime(tool.getUpdateTime())
                .build();
    }

    private AiTool getToolEntity(Long id) {
        AiTool tool = aiToolMapper.selectById(id);
        if (tool == null) {
            throw new BusinessException("AI工具不存在");
        }
        return tool;
    }

    private AiTool getEnabledToolEntity(Long id) {
        AiTool tool = aiToolMapper.selectOne(new LambdaQueryWrapper<AiTool>()
                .eq(AiTool::getId, id)
                .eq(AiTool::getStatus, STATUS_ENABLED)
                .last("limit 1"));
        if (tool == null) {
            throw new BusinessException("AI工具不存在或未启用");
        }
        return tool;
    }

    private void ensureToolCodeUnique(String code, Long excludeId) {
        AiTool existing = aiToolMapper.selectOne(new LambdaQueryWrapper<AiTool>()
                .eq(AiTool::getCode, code)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new BusinessException("工具编码已存在");
        }
    }

    private void ensureStageExists(Long stageId) {
        WorkflowStage stage = workflowStageMapper.selectById(stageId);
        if (stage == null || !Objects.equals(stage.getStatus(), STATUS_ENABLED)) {
            throw new BusinessException("工作流阶段不存在或未启用");
        }
    }

    private void ensureStagesExist(List<Long> stageIds) {
        if (stageIds.isEmpty()) {
            return;
        }
        List<WorkflowStage> stages = workflowStageMapper.selectBatchIds(stageIds);
        if (stages.size() != stageIds.size()) {
            throw new BusinessException("存在无效的工作流阶段ID");
        }
    }

    private void ensureDimensionsExist(List<Long> dimensionIds) {
        List<Long> normalizedIds = normalizeIds(dimensionIds, "维度ID不能为空");
        List<EvaluationDimension> dimensions = evaluationDimensionMapper.selectBatchIds(normalizedIds);
        if (dimensions.size() != normalizedIds.size()) {
            throw new BusinessException("存在无效的评分维度ID");
        }
    }

    private List<Long> normalizeIds(List<Long> ids, String nullMessage) {
        if (ids == null) {
            return Collections.emptyList();
        }
        List<Long> normalizedIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalizedIds.size() != ids.size()) {
            throw new BusinessException(nullMessage);
        }
        return normalizedIds;
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String normalizeDataStatus(String dataStatus, String defaultValue) {
        if (!StringUtils.hasText(dataStatus)) {
            return defaultValue;
        }
        return dataStatus.trim().toUpperCase();
    }
}
