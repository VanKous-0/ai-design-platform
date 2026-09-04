package com.project.modules.prompt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.common.exception.BusinessException;
import com.project.common.result.PageResult;
import com.project.common.util.PageSupport;
import com.project.modules.prompt.dto.PromptCreateRequest;
import com.project.modules.prompt.dto.PromptParameterCreateRequest;
import com.project.modules.prompt.dto.PromptParameterUpdateRequest;
import com.project.modules.prompt.dto.PromptPreferenceHintRequest;
import com.project.modules.prompt.dto.PromptRenderRequest;
import com.project.modules.prompt.dto.PromptToolSetRequest;
import com.project.modules.prompt.dto.PromptUpdateRequest;
import com.project.modules.prompt.entity.PromptParameter;
import com.project.modules.prompt.entity.PromptPreferenceHint;
import com.project.modules.prompt.entity.PromptTemplate;
import com.project.modules.prompt.entity.PromptToolRel;
import com.project.modules.prompt.entity.WorkflowNodePromptRel;
import com.project.modules.prompt.mapper.PromptParameterMapper;
import com.project.modules.prompt.mapper.PromptPreferenceHintMapper;
import com.project.modules.prompt.mapper.PromptTemplateMapper;
import com.project.modules.prompt.mapper.PromptToolRelMapper;
import com.project.modules.prompt.mapper.WorkflowNodePromptRelMapper;
import com.project.modules.prompt.service.PromptService;
import com.project.modules.prompt.service.PromptRevisionService;
import com.project.modules.prompt.vo.PromptDetailVO;
import com.project.modules.prompt.vo.PromptListVO;
import com.project.modules.prompt.vo.PromptParameterVO;
import com.project.modules.prompt.vo.PromptPreferenceHintVO;
import com.project.modules.prompt.vo.PromptRenderVO;
import com.project.modules.prompt.vo.PromptRevisionVO;
import com.project.modules.prompt.vo.PromptStageVO;
import com.project.modules.prompt.vo.PromptToolVO;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.mapper.AiToolMapper;
import com.project.modules.workflow.entity.WorkflowStage;
import com.project.modules.workflow.mapper.WorkflowStageMapper;
import com.project.modules.workflow.runtime.entity.WorkflowTemplateNode;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateNodeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PromptServiceImpl implements PromptService {

    private static final int STATUS_ENABLED = 1;
    private static final int DEFAULT_STATUS = 1;
    private static final int DEFAULT_SORT_ORDER = 0;
    private static final String DEFAULT_SOURCE_TYPE = "DEMO";

    private final PromptTemplateMapper promptTemplateMapper;
    private final PromptToolRelMapper promptToolRelMapper;
    private final PromptParameterMapper promptParameterMapper;
    private final PromptPreferenceHintMapper promptPreferenceHintMapper;
    private final WorkflowNodePromptRelMapper workflowNodePromptRelMapper;
    private final WorkflowTemplateNodeMapper workflowTemplateNodeMapper;
    private final WorkflowStageMapper workflowStageMapper;
    private final AiToolMapper aiToolMapper;
    private final PromptRevisionService promptRevisionService;

    public PromptServiceImpl(
            PromptTemplateMapper promptTemplateMapper,
            PromptToolRelMapper promptToolRelMapper,
            PromptParameterMapper promptParameterMapper,
            PromptPreferenceHintMapper promptPreferenceHintMapper,
            WorkflowNodePromptRelMapper workflowNodePromptRelMapper,
            WorkflowTemplateNodeMapper workflowTemplateNodeMapper,
            WorkflowStageMapper workflowStageMapper,
            AiToolMapper aiToolMapper,
            PromptRevisionService promptRevisionService
    ) {
        this.promptTemplateMapper = promptTemplateMapper;
        this.promptToolRelMapper = promptToolRelMapper;
        this.promptParameterMapper = promptParameterMapper;
        this.promptPreferenceHintMapper = promptPreferenceHintMapper;
        this.workflowNodePromptRelMapper = workflowNodePromptRelMapper;
        this.workflowTemplateNodeMapper = workflowTemplateNodeMapper;
        this.workflowStageMapper = workflowStageMapper;
        this.aiToolMapper = aiToolMapper;
        this.promptRevisionService = promptRevisionService;
    }

    @Override
    public List<PromptListVO> listPrompts(Long stageId, String category, String keyword, String sourceType) {
        return promptTemplateMapper.selectList(buildPromptQuery(stageId, category, keyword, sourceType))
                .stream()
                .map(this::toListVO)
                .toList();
    }

    @Override
    public PageResult<PromptListVO> pagePrompts(
            Long stageId,
            String category,
            String keyword,
            String sourceType,
            Long pageNum,
            Long pageSize
    ) {
        Page<PromptTemplate> page = PageSupport.page(pageNum, pageSize);
        Page<PromptTemplate> result = promptTemplateMapper.selectPage(
                page,
                buildPromptQuery(stageId, category, keyword, sourceType)
        );
        return PageResult.<PromptListVO>builder()
                .records(result.getRecords().stream().map(this::toListVO).toList())
                .total(result.getTotal())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .pages(result.getPages())
                .build();
    }

    private LambdaQueryWrapper<PromptTemplate> buildPromptQuery(
            Long stageId,
            String category,
            String keyword,
            String sourceType
    ) {
        LambdaQueryWrapper<PromptTemplate> queryWrapper = enabledPromptQuery()
                .orderByAsc(PromptTemplate::getSortOrder)
                .orderByAsc(PromptTemplate::getId);
        if (stageId != null) {
            ensureStageExists(stageId);
            queryWrapper.eq(PromptTemplate::getStageId, stageId);
        }
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(PromptTemplate::getCategory, category.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(PromptTemplate::getTitle, trimmedKeyword)
                    .or()
                    .like(PromptTemplate::getContent, trimmedKeyword));
        }
        if (StringUtils.hasText(sourceType)) {
            queryWrapper.eq(PromptTemplate::getSourceType, normalizeSourceType(sourceType));
        }
        return queryWrapper;
    }

    @Override
    public PromptDetailVO getPromptDetail(Long id) {
        PromptTemplate prompt = getEnabledPromptEntity(id);
        return toDetailVO(prompt);
    }

    @Override
    public List<PromptListVO> searchPrompts(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        return listPrompts(null, null, keyword, null);
    }

    @Override
    public List<PromptListVO> recommendPrompts(Long stageId, Long toolId) {
        if (stageId == null) {
            throw new BusinessException("阶段ID不能为空");
        }
        ensureStageExists(stageId);

        List<Long> promptIds = null;
        if (toolId != null) {
            ensureToolExists(toolId);
            promptIds = promptToolRelMapper.selectList(new LambdaQueryWrapper<PromptToolRel>()
                            .eq(PromptToolRel::getToolId, toolId))
                    .stream()
                    .map(PromptToolRel::getPromptId)
                    .distinct()
                    .toList();
            if (promptIds.isEmpty()) {
                return Collections.emptyList();
            }
        }

        LambdaQueryWrapper<PromptTemplate> queryWrapper = enabledPromptQuery()
                .eq(PromptTemplate::getStageId, stageId)
                .orderByAsc(PromptTemplate::getSortOrder)
                .orderByAsc(PromptTemplate::getId);
        if (promptIds != null) {
            queryWrapper.in(PromptTemplate::getId, promptIds);
        }
        return promptTemplateMapper.selectList(queryWrapper)
                .stream()
                .map(this::toListVO)
                .toList();
    }

    @Override
    public List<PromptListVO> listPromptsByNode(Long nodeId) {
        ensureWorkflowNodeExists(nodeId);
        List<Long> promptIds = workflowNodePromptRelMapper.selectList(new LambdaQueryWrapper<WorkflowNodePromptRel>()
                        .eq(WorkflowNodePromptRel::getNodeId, nodeId)
                        .orderByAsc(WorkflowNodePromptRel::getSortOrder)
                        .orderByAsc(WorkflowNodePromptRel::getId))
                .stream()
                .map(WorkflowNodePromptRel::getPromptId)
                .distinct()
                .toList();
        if (promptIds.isEmpty()) {
            return Collections.emptyList();
        }
        return promptTemplateMapper.selectList(enabledPromptQuery()
                        .in(PromptTemplate::getId, promptIds)
                        .orderByAsc(PromptTemplate::getSortOrder)
                        .orderByAsc(PromptTemplate::getId))
                .stream()
                .map(this::toListVO)
                .toList();
    }

    @Override
    public List<PromptParameterVO> listParameters(Long promptId) {
        getEnabledPromptEntity(promptId);
        return promptParameterMapper.selectList(new LambdaQueryWrapper<PromptParameter>()
                        .eq(PromptParameter::getPromptId, promptId)
                        .orderByAsc(PromptParameter::getSortOrder)
                        .orderByAsc(PromptParameter::getId))
                .stream()
                .map(this::toParameterVO)
                .toList();
    }

    @Override
    public PromptRenderVO renderPrompt(Long promptId, PromptRenderRequest request) {
        PromptTemplate prompt = getEnabledPromptEntity(promptId);
        Map<String, String> values = request == null || request.getParameters() == null
                ? Collections.emptyMap()
                : request.getParameters();
        Long revisionId = request == null ? null : request.getPromptRevisionId();
        return promptRevisionService.render(prompt, revisionId, values);
    }

    @Override
    public List<PromptRevisionVO> listRevisions(Long promptId) {
        getEnabledPromptEntity(promptId);
        return promptRevisionService.listRevisions(promptId);
    }

    @Override
    public PromptRevisionVO getRevision(Long promptId, Long revisionId) {
        getEnabledPromptEntity(promptId);
        return promptRevisionService.getRevision(promptId, revisionId);
    }

    @Override
    public void copyPrompt(Long id) {
        getEnabledPromptEntity(id);
        promptTemplateMapper.update(null, new LambdaUpdateWrapper<PromptTemplate>()
                .eq(PromptTemplate::getId, id)
                .setSql("copy_count = copy_count + 1")
                .set(PromptTemplate::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromptDetailVO createPrompt(PromptCreateRequest request, Long createdBy) {
        ensureStageExists(request.getStageId());
        ensurePromptCodeUnique(request.getCode(), null);

        LocalDateTime now = LocalDateTime.now();
        PromptTemplate prompt = new PromptTemplate();
        fillPrompt(prompt, request.getStageId(), request.getTitle(), request.getCode(), request.getCategory(),
                request.getContent(), request.getInputDesc(), request.getOutputDesc(), request.getTips(),
                request.getExampleInput(), request.getExampleOutput(), request.getSourceDesc(),
                request.getSourceType(), request.getSourceFile(), request.getSourcePage(),
                request.getSortOrder(), request.getStatus());
        prompt.setCopyCount(0);
        prompt.setOwnershipType("SYSTEM");
        prompt.setCreateTime(now);
        prompt.setUpdateTime(now);
        prompt.setIsDeleted(0);
        promptTemplateMapper.insert(prompt);
        replaceParameters(prompt.getId(), request.getParameters());
        replacePreferenceHints(prompt.getId(), request.getPreferenceHints());
        promptRevisionService.createRevision(prompt, createdBy);
        return toDetailVO(prompt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromptDetailVO updatePrompt(Long id, PromptUpdateRequest request, Long createdBy) {
        PromptTemplate prompt = getPromptEntityForUpdate(id);
        ensureStageExists(request.getStageId());
        ensurePromptCodeUnique(request.getCode(), id);

        fillPrompt(prompt, request.getStageId(), request.getTitle(), request.getCode(), request.getCategory(),
                request.getContent(), request.getInputDesc(), request.getOutputDesc(), request.getTips(),
                request.getExampleInput(), request.getExampleOutput(), request.getSourceDesc(),
                request.getSourceType(), request.getSourceFile(), request.getSourcePage(),
                request.getSortOrder(), request.getStatus());
        prompt.setUpdateTime(LocalDateTime.now());
        promptTemplateMapper.updateById(prompt);
        if (request.getParameters() != null) {
            replaceParameters(prompt.getId(), request.getParameters());
        }
        if (request.getPreferenceHints() != null) {
            replacePreferenceHints(prompt.getId(), request.getPreferenceHints());
        }
        promptRevisionService.createRevision(prompt, createdBy);
        return toDetailVO(prompt);
    }

    @Override
    public void deletePrompt(Long id) {
        getPromptEntity(id);
        promptTemplateMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> setPromptTools(Long promptId, PromptToolSetRequest request) {
        getPromptEntity(promptId);
        List<Long> toolIds = normalizeIds(request.getToolIds(), "工具ID不能为空");
        ensureToolsExist(toolIds);

        promptToolRelMapper.delete(new LambdaQueryWrapper<PromptToolRel>()
                .eq(PromptToolRel::getPromptId, promptId));

        LocalDateTime now = LocalDateTime.now();
        for (Long toolId : toolIds) {
            PromptToolRel rel = new PromptToolRel();
            rel.setPromptId(promptId);
            rel.setToolId(toolId);
            rel.setCreateTime(now);
            promptToolRelMapper.insert(rel);
        }
        return toolIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromptParameterVO createParameter(Long promptId, PromptParameterCreateRequest request, Long createdBy) {
        PromptTemplate prompt = getPromptEntityForUpdate(promptId);
        ensureParameterKeyUnique(promptId, request.getParamKey(), null);
        LocalDateTime now = LocalDateTime.now();
        PromptParameter parameter = insertParameter(promptId, request, now);
        promptRevisionService.createRevision(prompt, createdBy);
        return toParameterVO(parameter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromptParameterVO updateParameter(Long id, PromptParameterUpdateRequest request, Long createdBy) {
        PromptParameter parameter = getParameterEntity(id);
        PromptTemplate prompt = getPromptEntityForUpdate(parameter.getPromptId());
        ensureParameterKeyUnique(parameter.getPromptId(), request.getParamKey(), id);
        fillParameter(parameter, parameter.getPromptId(), request.getParamKey(), request.getParamName(), request.getParamType(),
                request.getRequired(), request.getDefaultValue(), request.getPlaceholder(), request.getSortOrder());
        parameter.setUpdateTime(LocalDateTime.now());
        promptParameterMapper.updateById(parameter);
        promptRevisionService.createRevision(prompt, createdBy);
        return toParameterVO(parameter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteParameter(Long id, Long createdBy) {
        PromptParameter parameter = getParameterEntity(id);
        PromptTemplate prompt = getPromptEntityForUpdate(parameter.getPromptId());
        promptParameterMapper.hardDeleteById(id);
        promptRevisionService.createRevision(prompt, createdBy);
    }

    private LambdaQueryWrapper<PromptTemplate> enabledPromptQuery() {
        return new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getStatus, STATUS_ENABLED)
                .ne(PromptTemplate::getSourceType, "DEMO");
    }

    private void fillPrompt(
            PromptTemplate prompt,
            Long stageId,
            String title,
            String code,
            String category,
            String content,
            String inputDesc,
            String outputDesc,
            String tips,
            String exampleInput,
            String exampleOutput,
            String sourceDesc,
            String sourceType,
            String sourceFile,
            String sourcePage,
            Integer sortOrder,
            Integer status
    ) {
        prompt.setStageId(stageId);
        prompt.setTitle(title);
        prompt.setCode(code);
        prompt.setCategory(category);
        prompt.setContent(content);
        prompt.setInputDesc(inputDesc);
        prompt.setOutputDesc(outputDesc);
        prompt.setTips(tips);
        prompt.setExampleInput(exampleInput);
        prompt.setExampleOutput(exampleOutput);
        prompt.setSourceDesc(sourceDesc);
        prompt.setSourceType(StringUtils.hasText(sourceType)
                ? normalizeSourceType(sourceType)
                : DEFAULT_SOURCE_TYPE);
        prompt.setSourceFile(sourceFile);
        prompt.setSourcePage(sourcePage);
        prompt.setSortOrder(defaultIfNull(sortOrder, DEFAULT_SORT_ORDER));
        prompt.setStatus(defaultIfNull(status, DEFAULT_STATUS));
    }

    private PromptTemplate getPromptEntity(Long id) {
        PromptTemplate prompt = promptTemplateMapper.selectById(id);
        if (prompt == null) {
            throw new BusinessException("提示词不存在");
        }
        return prompt;
    }

    private PromptTemplate getPromptEntityForUpdate(Long id) {
        PromptTemplate prompt = promptTemplateMapper.selectByIdForUpdate(id);
        if (prompt == null) {
            throw new BusinessException("提示词不存在");
        }
        return prompt;
    }

    private PromptTemplate getEnabledPromptEntity(Long id) {
        PromptTemplate prompt = promptTemplateMapper.selectOne(enabledPromptQuery()
                .eq(PromptTemplate::getId, id)
                .last("limit 1"));
        if (prompt == null) {
            throw new BusinessException("提示词不存在或未启用");
        }
        return prompt;
    }

    private void ensurePromptCodeUnique(String code, Long excludeId) {
        PromptTemplate existing = promptTemplateMapper.selectOne(new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getCode, code)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new BusinessException("提示词编码已存在");
        }
    }

    private void ensureStageExists(Long stageId) {
        WorkflowStage stage = workflowStageMapper.selectById(stageId);
        if (stage == null || !Objects.equals(stage.getStatus(), STATUS_ENABLED)) {
            throw new BusinessException("工作流阶段不存在或未启用");
        }
    }

    private void ensureToolExists(Long toolId) {
        AiTool tool = aiToolMapper.selectById(toolId);
        if (tool == null || !Objects.equals(tool.getStatus(), STATUS_ENABLED)) {
            throw new BusinessException("AI工具不存在或未启用");
        }
    }

    private void ensureToolsExist(List<Long> toolIds) {
        if (toolIds.isEmpty()) {
            return;
        }
        List<AiTool> tools = aiToolMapper.selectBatchIds(toolIds);
        Set<Long> enabledToolIds = tools.stream()
                .filter(tool -> Objects.equals(tool.getStatus(), STATUS_ENABLED))
                .map(AiTool::getId)
                .collect(Collectors.toSet());
        if (enabledToolIds.size() != toolIds.size()) {
            throw new BusinessException("存在无效或未启用的AI工具ID");
        }
    }

    private void ensureWorkflowNodeExists(Long nodeId) {
        WorkflowTemplateNode node = workflowTemplateNodeMapper.selectById(nodeId);
        if (node == null || !Objects.equals(node.getStatus(), STATUS_ENABLED)) {
            throw new BusinessException("Workflow node does not exist or is disabled");
        }
    }

    private PromptParameter getParameterEntity(Long id) {
        PromptParameter parameter = promptParameterMapper.selectById(id);
        if (parameter == null) {
            throw new BusinessException("Prompt parameter does not exist");
        }
        return parameter;
    }

    private void ensureParameterKeyUnique(Long promptId, String paramKey, Long excludeId) {
        PromptParameter existing = promptParameterMapper.selectOne(new LambdaQueryWrapper<PromptParameter>()
                .eq(PromptParameter::getPromptId, promptId)
                .eq(PromptParameter::getParamKey, paramKey)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new BusinessException("Prompt parameter key already exists");
        }
    }

    private void fillParameter(
            PromptParameter parameter,
            Long promptId,
            String paramKey,
            String paramName,
            String paramType,
            Boolean required,
            String defaultValue,
            String placeholder,
            Integer sortOrder
    ) {
        parameter.setPromptId(promptId);
        parameter.setParamKey(paramKey);
        parameter.setParamName(paramName);
        parameter.setParamType(StringUtils.hasText(paramType) ? paramType : "text");
        parameter.setRequired(Boolean.TRUE.equals(required) ? 1 : 0);
        parameter.setDefaultValue(defaultValue);
        parameter.setPlaceholder(placeholder);
        parameter.setSortOrder(defaultIfNull(sortOrder, DEFAULT_SORT_ORDER));
    }

    private void replaceParameters(Long promptId, List<PromptParameterCreateRequest> requests) {
        if (requests == null) {
            return;
        }
        Set<String> keys = requests.stream()
                .map(PromptParameterCreateRequest::getParamKey)
                .collect(Collectors.toSet());
        if (keys.size() != requests.size()) {
            throw new BusinessException("Prompt parameter keys must be unique");
        }
        promptParameterMapper.hardDeleteByPromptId(promptId);
        LocalDateTime now = LocalDateTime.now();
        requests.forEach(request -> insertParameter(promptId, request, now));
    }

    private void replacePreferenceHints(Long promptId, List<PromptPreferenceHintRequest> requests) {
        if (requests == null) {
            return;
        }
        List<String> keys = requests.stream()
                .map(request -> normalizePreferenceKey(request.getPreferenceKey()))
                .toList();
        if (keys.stream().distinct().count() != keys.size()) {
            throw new BusinessException("Prompt preference hint keys must be unique");
        }

        promptPreferenceHintMapper.hardDeleteByPromptId(promptId);
        LocalDateTime now = LocalDateTime.now();
        for (int index = 0; index < requests.size(); index++) {
            PromptPreferenceHintRequest request = requests.get(index);
            PromptPreferenceHint hint = new PromptPreferenceHint();
            hint.setPromptId(promptId);
            hint.setPreferenceKey(keys.get(index));
            hint.setPreferenceValue(request.getPreferenceValue().trim());
            hint.setCreateTime(now);
            hint.setUpdateTime(now);
            hint.setIsDeleted(0);
            promptPreferenceHintMapper.insert(hint);
        }
    }

    private String normalizePreferenceKey(String preferenceKey) {
        if (!StringUtils.hasText(preferenceKey)) {
            throw new BusinessException("Preference key cannot be blank");
        }
        String normalized = preferenceKey.trim();
        if (!normalized.matches("[a-z0-9][a-z0-9_.-]{0,79}")) {
            throw new BusinessException("Preference key must be a stable lowercase identifier");
        }
        return normalized;
    }

    private PromptParameter insertParameter(
            Long promptId,
            PromptParameterCreateRequest request,
            LocalDateTime now
    ) {
        PromptParameter parameter = new PromptParameter();
        fillParameter(parameter, promptId, request.getParamKey(), request.getParamName(), request.getParamType(),
                request.getRequired(), request.getDefaultValue(), request.getPlaceholder(), request.getSortOrder());
        parameter.setCreateTime(now);
        parameter.setUpdateTime(now);
        parameter.setIsDeleted(0);
        promptParameterMapper.insert(parameter);
        return parameter;
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

    private PromptListVO toListVO(PromptTemplate prompt) {
        return PromptListVO.builder()
                .id(prompt.getId())
                .stageId(prompt.getStageId())
                .ownerUserId(prompt.getOwnerUserId())
                .ownershipType(prompt.getOwnershipType())
                .currentRevisionId(prompt.getCurrentRevisionId())
                .title(prompt.getTitle())
                .code(prompt.getCode())
                .category(prompt.getCategory())
                .content(prompt.getContent())
                .sourceType(prompt.getSourceType())
                .sourceFile(prompt.getSourceFile())
                .sourcePage(prompt.getSourcePage())
                .sortOrder(prompt.getSortOrder())
                .copyCount(prompt.getCopyCount())
                .status(prompt.getStatus())
                .createTime(prompt.getCreateTime())
                .updateTime(prompt.getUpdateTime())
                .build();
    }

    private PromptParameterVO toParameterVO(PromptParameter parameter) {
        return PromptParameterVO.builder()
                .id(parameter.getId())
                .promptId(parameter.getPromptId())
                .paramKey(parameter.getParamKey())
                .paramName(parameter.getParamName())
                .paramType(parameter.getParamType())
                .required(Objects.equals(parameter.getRequired(), 1))
                .defaultValue(parameter.getDefaultValue())
                .placeholder(parameter.getPlaceholder())
                .sortOrder(parameter.getSortOrder())
                .createTime(parameter.getCreateTime())
                .updateTime(parameter.getUpdateTime())
                .build();
    }

    private PromptDetailVO toDetailVO(PromptTemplate prompt) {
        return PromptDetailVO.builder()
                .id(prompt.getId())
                .stageId(prompt.getStageId())
                .ownerUserId(prompt.getOwnerUserId())
                .ownershipType(prompt.getOwnershipType())
                .currentRevisionId(prompt.getCurrentRevisionId())
                .title(prompt.getTitle())
                .code(prompt.getCode())
                .category(prompt.getCategory())
                .content(prompt.getContent())
                .inputDesc(prompt.getInputDesc())
                .outputDesc(prompt.getOutputDesc())
                .tips(prompt.getTips())
                .exampleInput(prompt.getExampleInput())
                .exampleOutput(prompt.getExampleOutput())
                .sourceDesc(prompt.getSourceDesc())
                .sourceType(prompt.getSourceType())
                .sourceFile(prompt.getSourceFile())
                .sourcePage(prompt.getSourcePage())
                .sortOrder(prompt.getSortOrder())
                .copyCount(prompt.getCopyCount())
                .status(prompt.getStatus())
                .stage(loadStage(prompt.getStageId()))
                .tools(loadTools(prompt.getId()))
                .preferenceHints(loadPreferenceHints(prompt.getId()))
                .createTime(prompt.getCreateTime())
                .updateTime(prompt.getUpdateTime())
                .build();
    }

    private PromptStageVO loadStage(Long stageId) {
        WorkflowStage stage = workflowStageMapper.selectById(stageId);
        if (stage == null) {
            return null;
        }
        return PromptStageVO.builder()
                .id(stage.getId())
                .name(stage.getName())
                .code(stage.getCode())
                .build();
    }

    private List<PromptToolVO> loadTools(Long promptId) {
        List<Long> toolIds = promptToolRelMapper.selectList(new LambdaQueryWrapper<PromptToolRel>()
                        .eq(PromptToolRel::getPromptId, promptId))
                .stream()
                .map(PromptToolRel::getToolId)
                .toList();
        if (toolIds.isEmpty()) {
            return Collections.emptyList();
        }
        return aiToolMapper.selectBatchIds(toolIds)
                .stream()
                .filter(tool -> Objects.equals(tool.getStatus(), STATUS_ENABLED))
                .collect(Collectors.toMap(AiTool::getId, Function.identity(), (first, second) -> first))
                .values()
                .stream()
                .map(tool -> PromptToolVO.builder()
                        .id(tool.getId())
                        .name(tool.getName())
                        .code(tool.getCode())
                        .officialUrl(tool.getOfficialUrl())
                        .logoUrl(tool.getLogoUrl())
                        .build())
                .toList();
    }

    private List<PromptPreferenceHintVO> loadPreferenceHints(Long promptId) {
        return promptPreferenceHintMapper.selectList(new LambdaQueryWrapper<PromptPreferenceHint>()
                        .eq(PromptPreferenceHint::getPromptId, promptId)
                        .orderByAsc(PromptPreferenceHint::getPreferenceKey)
                        .orderByAsc(PromptPreferenceHint::getId))
                .stream()
                .map(hint -> PromptPreferenceHintVO.builder()
                        .id(hint.getId())
                        .preferenceKey(hint.getPreferenceKey())
                        .preferenceValue(hint.getPreferenceValue())
                        .build())
                .toList();
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String normalizeSourceType(String sourceType) {
        String normalized = sourceType.trim().toUpperCase();
        if (!Set.of("ORIGINAL", "RECONSTRUCTED", "DEMO").contains(normalized)) {
            throw new BusinessException("来源类型仅支持 ORIGINAL、RECONSTRUCTED 或 DEMO");
        }
        return normalized;
    }
}
