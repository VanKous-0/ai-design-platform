package com.project.modules.prompt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.exception.BusinessException;
import com.project.modules.prompt.entity.PromptParameter;
import com.project.modules.prompt.entity.PromptRevision;
import com.project.modules.prompt.entity.PromptTemplate;
import com.project.modules.prompt.mapper.PromptParameterMapper;
import com.project.modules.prompt.mapper.PromptRevisionMapper;
import com.project.modules.prompt.mapper.PromptTemplateMapper;
import com.project.modules.prompt.model.PromptParameterSnapshot;
import com.project.modules.prompt.service.PromptRevisionService;
import com.project.modules.prompt.vo.PromptRenderVO;
import com.project.modules.prompt.vo.PromptRevisionVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PromptRevisionServiceImpl implements PromptRevisionService {

    private static final String REVISION_PUBLISHED = "PUBLISHED";

    private final PromptRevisionMapper revisionMapper;
    private final PromptParameterMapper parameterMapper;
    private final PromptTemplateMapper templateMapper;
    private final ObjectMapper objectMapper;

    public PromptRevisionServiceImpl(
            PromptRevisionMapper revisionMapper,
            PromptParameterMapper parameterMapper,
            PromptTemplateMapper templateMapper,
            ObjectMapper objectMapper
    ) {
        this.revisionMapper = revisionMapper;
        this.parameterMapper = parameterMapper;
        this.templateMapper = templateMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PromptRevision createRevision(PromptTemplate prompt, Long createdBy) {
        List<PromptParameterSnapshot> parameterSchema = parameterMapper.selectList(
                        new LambdaQueryWrapper<PromptParameter>()
                                .eq(PromptParameter::getPromptId, prompt.getId())
                                .orderByAsc(PromptParameter::getSortOrder)
                                .orderByAsc(PromptParameter::getId)
                ).stream()
                .map(this::toSnapshot)
                .toList();

        LocalDateTime now = LocalDateTime.now();
        PromptRevision revision = new PromptRevision();
        revision.setPromptId(prompt.getId());
        revision.setRevisionNo(revisionMapper.selectMaxRevisionNo(prompt.getId()) + 1);
        revision.setContent(prompt.getContent());
        revision.setInputDesc(prompt.getInputDesc());
        revision.setOutputDesc(prompt.getOutputDesc());
        revision.setTips(prompt.getTips());
        revision.setExampleInput(prompt.getExampleInput());
        revision.setExampleOutput(prompt.getExampleOutput());
        revision.setParameterSchemaJson(writeSchema(parameterSchema));
        revision.setCreatedBy(createdBy);
        revision.setStatus(REVISION_PUBLISHED);
        revision.setCreateTime(now);
        revisionMapper.insert(revision);

        prompt.setCurrentRevisionId(revision.getId());
        prompt.setUpdateTime(now);
        templateMapper.updateById(prompt);
        return revision;
    }

    @Override
    public PromptRevision requireRevision(Long promptId, Long revisionId) {
        PromptRevision revision = revisionMapper.selectOne(new LambdaQueryWrapper<PromptRevision>()
                .eq(PromptRevision::getId, revisionId)
                .eq(PromptRevision::getPromptId, promptId)
                .last("limit 1"));
        if (revision == null) {
            throw new BusinessException("Prompt revision does not exist or does not belong to this prompt");
        }
        return revision;
    }

    @Override
    public PromptRenderVO render(PromptTemplate prompt, Long revisionId, Map<String, String> values) {
        Long selectedRevisionId = revisionId == null ? prompt.getCurrentRevisionId() : revisionId;
        PromptRevision revision = requireRevision(prompt.getId(), selectedRevisionId);
        List<PromptParameterSnapshot> parameters = readSchema(revision.getParameterSchemaJson()).stream()
                .sorted(Comparator.comparing(
                                PromptParameterSnapshot::sortOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        ).thenComparing(PromptParameterSnapshot::paramKey))
                .toList();

        String renderedContent = revision.getContent();
        List<String> missingRequiredParams = new ArrayList<>();
        for (PromptParameterSnapshot parameter : parameters) {
            String value = values.get(parameter.paramKey());
            if (!StringUtils.hasText(value)) {
                value = parameter.defaultValue();
            }
            if (!StringUtils.hasText(value) && Objects.equals(parameter.required(), 1)) {
                missingRequiredParams.add(parameter.paramKey());
                continue;
            }
            if (value != null) {
                renderedContent = renderedContent
                        .replace("{{" + parameter.paramKey() + "}}", value)
                        .replace("{" + parameter.paramKey() + "}", value);
            }
        }
        if (!missingRequiredParams.isEmpty()) {
            throw new BusinessException("Missing required prompt parameters: "
                    + String.join(",", missingRequiredParams));
        }
        return PromptRenderVO.builder()
                .promptId(prompt.getId())
                .promptRevisionId(revision.getId())
                .revisionNo(revision.getRevisionNo())
                .title(prompt.getTitle())
                .renderedContent(renderedContent)
                .missingRequiredParams(missingRequiredParams)
                .build();
    }

    @Override
    public List<PromptRevisionVO> listRevisions(Long promptId) {
        return revisionMapper.selectList(new LambdaQueryWrapper<PromptRevision>()
                        .eq(PromptRevision::getPromptId, promptId)
                        .orderByDesc(PromptRevision::getRevisionNo))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public PromptRevisionVO getRevision(Long promptId, Long revisionId) {
        return toVO(requireRevision(promptId, revisionId));
    }

    private PromptParameterSnapshot toSnapshot(PromptParameter parameter) {
        return new PromptParameterSnapshot(
                parameter.getParamKey(),
                parameter.getParamName(),
                parameter.getParamType(),
                parameter.getRequired(),
                parameter.getDefaultValue(),
                parameter.getPlaceholder(),
                parameter.getSortOrder()
        );
    }

    private PromptRevisionVO toVO(PromptRevision revision) {
        return PromptRevisionVO.builder()
                .id(revision.getId())
                .promptId(revision.getPromptId())
                .revisionNo(revision.getRevisionNo())
                .content(revision.getContent())
                .inputDesc(revision.getInputDesc())
                .outputDesc(revision.getOutputDesc())
                .tips(revision.getTips())
                .exampleInput(revision.getExampleInput())
                .exampleOutput(revision.getExampleOutput())
                .parameterSchema(readSchema(revision.getParameterSchemaJson()))
                .createdBy(revision.getCreatedBy())
                .status(revision.getStatus())
                .createTime(revision.getCreateTime())
                .build();
    }

    private String writeSchema(List<PromptParameterSnapshot> parameterSchema) {
        try {
            return objectMapper.writeValueAsString(parameterSchema);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize prompt parameter schema", ex);
        }
    }

    private List<PromptParameterSnapshot> readSchema(String parameterSchemaJson) {
        try {
            return objectMapper.readValue(
                    parameterSchemaJson,
                    new TypeReference<List<PromptParameterSnapshot>>() { }
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Stored prompt parameter schema is invalid", ex);
        }
    }
}
