package com.project.modules.prompt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.project.common.exception.BusinessException;
import com.project.modules.prompt.dto.PromptCreateRequest;
import com.project.modules.prompt.dto.PromptToolSetRequest;
import com.project.modules.prompt.dto.PromptUpdateRequest;
import com.project.modules.prompt.entity.PromptTemplate;
import com.project.modules.prompt.entity.PromptToolRel;
import com.project.modules.prompt.mapper.PromptTemplateMapper;
import com.project.modules.prompt.mapper.PromptToolRelMapper;
import com.project.modules.prompt.service.PromptService;
import com.project.modules.prompt.vo.PromptDetailVO;
import com.project.modules.prompt.vo.PromptListVO;
import com.project.modules.prompt.vo.PromptStageVO;
import com.project.modules.prompt.vo.PromptToolVO;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.mapper.AiToolMapper;
import com.project.modules.workflow.entity.WorkflowStage;
import com.project.modules.workflow.mapper.WorkflowStageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PromptServiceImpl implements PromptService {

    private static final int STATUS_ENABLED = 1;
    private static final int DEFAULT_STATUS = 1;
    private static final int DEFAULT_SORT_ORDER = 0;

    private final PromptTemplateMapper promptTemplateMapper;
    private final PromptToolRelMapper promptToolRelMapper;
    private final WorkflowStageMapper workflowStageMapper;
    private final AiToolMapper aiToolMapper;

    public PromptServiceImpl(
            PromptTemplateMapper promptTemplateMapper,
            PromptToolRelMapper promptToolRelMapper,
            WorkflowStageMapper workflowStageMapper,
            AiToolMapper aiToolMapper
    ) {
        this.promptTemplateMapper = promptTemplateMapper;
        this.promptToolRelMapper = promptToolRelMapper;
        this.workflowStageMapper = workflowStageMapper;
        this.aiToolMapper = aiToolMapper;
    }

    @Override
    public List<PromptListVO> listPrompts(Long stageId, String category, String keyword) {
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
        return promptTemplateMapper.selectList(queryWrapper)
                .stream()
                .map(this::toListVO)
                .toList();
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
        return listPrompts(null, null, keyword);
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
    public void copyPrompt(Long id) {
        getEnabledPromptEntity(id);
        promptTemplateMapper.update(null, new LambdaUpdateWrapper<PromptTemplate>()
                .eq(PromptTemplate::getId, id)
                .setSql("copy_count = copy_count + 1")
                .set(PromptTemplate::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public PromptDetailVO createPrompt(PromptCreateRequest request) {
        ensureStageExists(request.getStageId());
        ensurePromptCodeUnique(request.getCode(), null);

        LocalDateTime now = LocalDateTime.now();
        PromptTemplate prompt = new PromptTemplate();
        fillPrompt(prompt, request.getStageId(), request.getTitle(), request.getCode(), request.getCategory(),
                request.getContent(), request.getInputDesc(), request.getOutputDesc(), request.getTips(),
                request.getExampleInput(), request.getExampleOutput(), request.getSortOrder(), request.getStatus());
        prompt.setCopyCount(0);
        prompt.setCreateTime(now);
        prompt.setUpdateTime(now);
        prompt.setIsDeleted(0);
        promptTemplateMapper.insert(prompt);
        return toDetailVO(prompt);
    }

    @Override
    public PromptDetailVO updatePrompt(Long id, PromptUpdateRequest request) {
        PromptTemplate prompt = getPromptEntity(id);
        ensureStageExists(request.getStageId());
        ensurePromptCodeUnique(request.getCode(), id);

        fillPrompt(prompt, request.getStageId(), request.getTitle(), request.getCode(), request.getCategory(),
                request.getContent(), request.getInputDesc(), request.getOutputDesc(), request.getTips(),
                request.getExampleInput(), request.getExampleOutput(), request.getSortOrder(), request.getStatus());
        prompt.setUpdateTime(LocalDateTime.now());
        promptTemplateMapper.updateById(prompt);
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

    private LambdaQueryWrapper<PromptTemplate> enabledPromptQuery() {
        return new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getStatus, STATUS_ENABLED);
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
                .title(prompt.getTitle())
                .code(prompt.getCode())
                .category(prompt.getCategory())
                .content(prompt.getContent())
                .sortOrder(prompt.getSortOrder())
                .copyCount(prompt.getCopyCount())
                .status(prompt.getStatus())
                .createTime(prompt.getCreateTime())
                .updateTime(prompt.getUpdateTime())
                .build();
    }

    private PromptDetailVO toDetailVO(PromptTemplate prompt) {
        return PromptDetailVO.builder()
                .id(prompt.getId())
                .stageId(prompt.getStageId())
                .title(prompt.getTitle())
                .code(prompt.getCode())
                .category(prompt.getCategory())
                .content(prompt.getContent())
                .inputDesc(prompt.getInputDesc())
                .outputDesc(prompt.getOutputDesc())
                .tips(prompt.getTips())
                .exampleInput(prompt.getExampleInput())
                .exampleOutput(prompt.getExampleOutput())
                .sortOrder(prompt.getSortOrder())
                .copyCount(prompt.getCopyCount())
                .status(prompt.getStatus())
                .stage(loadStage(prompt.getStageId()))
                .tools(loadTools(prompt.getId()))
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

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
