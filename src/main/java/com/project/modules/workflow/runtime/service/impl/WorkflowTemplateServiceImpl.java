package com.project.modules.workflow.runtime.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.common.exception.BusinessException;
import com.project.modules.prompt.entity.PromptTemplate;
import com.project.modules.prompt.entity.WorkflowNodePromptRel;
import com.project.modules.prompt.mapper.PromptTemplateMapper;
import com.project.modules.prompt.mapper.WorkflowNodePromptRelMapper;
import com.project.modules.workflow.entity.WorkflowStage;
import com.project.modules.workflow.entity.WorkflowStep;
import com.project.modules.workflow.mapper.WorkflowStageMapper;
import com.project.modules.workflow.mapper.WorkflowStepMapper;
import com.project.modules.workflow.runtime.dto.WorkflowNodePromptSetRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateNodeCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateNodeUpdateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateUpdateRequest;
import com.project.modules.workflow.runtime.entity.WorkflowTemplate;
import com.project.modules.workflow.runtime.entity.WorkflowTemplateNode;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateNodeMapper;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateListVO;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateNodeVO;
import com.project.modules.workflow.runtime.service.WorkflowTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class WorkflowTemplateServiceImpl implements WorkflowTemplateService {

    private static final int STATUS_ENABLED = 1;
    private static final int DEFAULT_STATUS = 1;
    private static final int DEFAULT_SORT_ORDER = 0;

    private final WorkflowTemplateMapper templateMapper;
    private final WorkflowTemplateNodeMapper nodeMapper;
    private final WorkflowStageMapper stageMapper;
    private final WorkflowStepMapper stepMapper;
    private final PromptTemplateMapper promptTemplateMapper;
    private final WorkflowNodePromptRelMapper nodePromptRelMapper;

    public WorkflowTemplateServiceImpl(
            WorkflowTemplateMapper templateMapper,
            WorkflowTemplateNodeMapper nodeMapper,
            WorkflowStageMapper stageMapper,
            WorkflowStepMapper stepMapper,
            PromptTemplateMapper promptTemplateMapper,
            WorkflowNodePromptRelMapper nodePromptRelMapper
    ) {
        this.templateMapper = templateMapper;
        this.nodeMapper = nodeMapper;
        this.stageMapper = stageMapper;
        this.stepMapper = stepMapper;
        this.promptTemplateMapper = promptTemplateMapper;
        this.nodePromptRelMapper = nodePromptRelMapper;
    }

    @Override
    public List<WorkflowTemplateListVO> listEnabledTemplates() {
        return templateMapper.selectList(enabledTemplateQuery()
                        .orderByAsc(WorkflowTemplate::getSortOrder)
                        .orderByAsc(WorkflowTemplate::getId))
                .stream()
                .map(this::toListVO)
                .toList();
    }

    @Override
    public WorkflowTemplateDetailVO getEnabledTemplate(Long id) {
        WorkflowTemplate template = templateMapper.selectOne(enabledTemplateQuery()
                .eq(WorkflowTemplate::getId, id)
                .last("limit 1"));
        if (template == null) {
            throw new BusinessException("Workflow template does not exist or is disabled");
        }
        return toDetailVO(template, true);
    }

    @Override
    public WorkflowTemplateDetailVO createTemplate(WorkflowTemplateCreateRequest request) {
        ensureTemplateCodeUnique(request.getCode(), null);
        LocalDateTime now = LocalDateTime.now();
        WorkflowTemplate template = new WorkflowTemplate();
        fillTemplate(template, request.getName(), request.getCode(), request.getDescription(),
                request.getSceneType(), request.getCoverUrl(), request.getSortOrder(), request.getStatus());
        template.setCreateTime(now);
        template.setUpdateTime(now);
        template.setIsDeleted(0);
        templateMapper.insert(template);
        return toDetailVO(template, false);
    }

    @Override
    public WorkflowTemplateDetailVO updateTemplate(Long id, WorkflowTemplateUpdateRequest request) {
        WorkflowTemplate template = getTemplateEntity(id);
        ensureTemplateCodeUnique(request.getCode(), id);
        fillTemplate(template, request.getName(), request.getCode(), request.getDescription(),
                request.getSceneType(), request.getCoverUrl(), request.getSortOrder(), request.getStatus());
        template.setUpdateTime(LocalDateTime.now());
        templateMapper.updateById(template);
        return toDetailVO(template, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id) {
        getTemplateEntity(id);
        templateMapper.deleteById(id);
        nodeMapper.delete(new LambdaQueryWrapper<WorkflowTemplateNode>().eq(WorkflowTemplateNode::getTemplateId, id));
    }

    @Override
    public WorkflowTemplateNodeVO createNode(WorkflowTemplateNodeCreateRequest request) {
        getTemplateEntity(request.getTemplateId());
        ensureRelatedStageAndStep(request.getStageId(), request.getStepId());
        ensureNodeCodeUnique(request.getTemplateId(), request.getNodeCode(), null);

        LocalDateTime now = LocalDateTime.now();
        WorkflowTemplateNode node = new WorkflowTemplateNode();
        fillNode(node, request.getTemplateId(), request.getStageId(), request.getStepId(), request.getNodeName(),
                request.getNodeCode(), request.getNodeType(), request.getInputDesc(), request.getOutputDesc(),
                request.getNextTip(), request.getSortOrder(), request.getStatus());
        node.setCreateTime(now);
        node.setUpdateTime(now);
        node.setIsDeleted(0);
        nodeMapper.insert(node);
        return toNodeVO(node);
    }

    @Override
    public WorkflowTemplateNodeVO updateNode(Long id, WorkflowTemplateNodeUpdateRequest request) {
        WorkflowTemplateNode node = getNodeEntity(id);
        getTemplateEntity(request.getTemplateId());
        ensureRelatedStageAndStep(request.getStageId(), request.getStepId());
        ensureNodeCodeUnique(request.getTemplateId(), request.getNodeCode(), id);

        fillNode(node, request.getTemplateId(), request.getStageId(), request.getStepId(), request.getNodeName(),
                request.getNodeCode(), request.getNodeType(), request.getInputDesc(), request.getOutputDesc(),
                request.getNextTip(), request.getSortOrder(), request.getStatus());
        node.setUpdateTime(LocalDateTime.now());
        nodeMapper.updateById(node);
        return toNodeVO(node);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNode(Long id) {
        getNodeEntity(id);
        nodeMapper.deleteById(id);
        nodePromptRelMapper.delete(new LambdaQueryWrapper<WorkflowNodePromptRel>()
                .eq(WorkflowNodePromptRel::getNodeId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> setNodePrompts(Long nodeId, WorkflowNodePromptSetRequest request) {
        getNodeEntity(nodeId);
        List<Long> promptIds = normalizeIds(request.getPromptIds());
        ensurePromptsExist(promptIds);

        nodePromptRelMapper.delete(new LambdaQueryWrapper<WorkflowNodePromptRel>()
                .eq(WorkflowNodePromptRel::getNodeId, nodeId));
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < promptIds.size(); i++) {
            WorkflowNodePromptRel rel = new WorkflowNodePromptRel();
            rel.setNodeId(nodeId);
            rel.setPromptId(promptIds.get(i));
            rel.setSortOrder(i + 1);
            rel.setCreateTime(now);
            nodePromptRelMapper.insert(rel);
        }
        return promptIds;
    }

    private LambdaQueryWrapper<WorkflowTemplate> enabledTemplateQuery() {
        return new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getStatus, STATUS_ENABLED);
    }

    private List<WorkflowTemplateNode> listNodes(Long templateId, boolean enabledOnly) {
        LambdaQueryWrapper<WorkflowTemplateNode> query = new LambdaQueryWrapper<WorkflowTemplateNode>()
                .eq(WorkflowTemplateNode::getTemplateId, templateId)
                .orderByAsc(WorkflowTemplateNode::getSortOrder)
                .orderByAsc(WorkflowTemplateNode::getId);
        if (enabledOnly) {
            query.eq(WorkflowTemplateNode::getStatus, STATUS_ENABLED);
        }
        return nodeMapper.selectList(query);
    }

    private WorkflowTemplate getTemplateEntity(Long id) {
        WorkflowTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("Workflow template does not exist");
        }
        return template;
    }

    private WorkflowTemplateNode getNodeEntity(Long id) {
        WorkflowTemplateNode node = nodeMapper.selectById(id);
        if (node == null) {
            throw new BusinessException("Workflow template node does not exist");
        }
        return node;
    }

    private void ensureTemplateCodeUnique(String code, Long excludeId) {
        WorkflowTemplate existing = templateMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getCode, code)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new BusinessException("Workflow template code already exists");
        }
    }

    private void ensureNodeCodeUnique(Long templateId, String nodeCode, Long excludeId) {
        WorkflowTemplateNode existing = nodeMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplateNode>()
                .eq(WorkflowTemplateNode::getTemplateId, templateId)
                .eq(WorkflowTemplateNode::getNodeCode, nodeCode)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new BusinessException("Workflow node code already exists in this template");
        }
    }

    private void ensureRelatedStageAndStep(Long stageId, Long stepId) {
        if (stageId != null) {
            WorkflowStage stage = stageMapper.selectById(stageId);
            if (stage == null) {
                throw new BusinessException("Related workflow stage does not exist");
            }
        }
        if (stepId != null) {
            WorkflowStep step = stepMapper.selectById(stepId);
            if (step == null) {
                throw new BusinessException("Related workflow step does not exist");
            }
            if (stageId != null && !Objects.equals(step.getStageId(), stageId)) {
                throw new BusinessException("Related workflow step does not belong to the stage");
            }
        }
    }

    private void ensurePromptsExist(List<Long> promptIds) {
        if (promptIds.isEmpty()) {
            return;
        }
        List<PromptTemplate> prompts = promptTemplateMapper.selectBatchIds(promptIds);
        long enabledCount = prompts.stream()
                .filter(prompt -> Objects.equals(prompt.getStatus(), STATUS_ENABLED))
                .count();
        if (enabledCount != promptIds.size()) {
            throw new BusinessException("Some prompts do not exist or are disabled");
        }
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        List<Long> normalized = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (normalized.size() != ids.size()) {
            throw new BusinessException("Prompt IDs cannot contain null");
        }
        return normalized;
    }

    private void fillTemplate(
            WorkflowTemplate template,
            String name,
            String code,
            String description,
            String sceneType,
            String coverUrl,
            Integer sortOrder,
            Integer status
    ) {
        template.setName(name);
        template.setCode(code);
        template.setDescription(description);
        template.setSceneType(sceneType);
        template.setCoverUrl(coverUrl);
        template.setSortOrder(defaultIfNull(sortOrder, DEFAULT_SORT_ORDER));
        template.setStatus(defaultIfNull(status, DEFAULT_STATUS));
    }

    private void fillNode(
            WorkflowTemplateNode node,
            Long templateId,
            Long stageId,
            Long stepId,
            String nodeName,
            String nodeCode,
            String nodeType,
            String inputDesc,
            String outputDesc,
            String nextTip,
            Integer sortOrder,
            Integer status
    ) {
        node.setTemplateId(templateId);
        node.setStageId(stageId);
        node.setStepId(stepId);
        node.setNodeName(nodeName);
        node.setNodeCode(nodeCode);
        node.setNodeType(nodeType);
        node.setInputDesc(inputDesc);
        node.setOutputDesc(outputDesc);
        node.setNextTip(nextTip);
        node.setSortOrder(defaultIfNull(sortOrder, DEFAULT_SORT_ORDER));
        node.setStatus(defaultIfNull(status, DEFAULT_STATUS));
    }

    private WorkflowTemplateListVO toListVO(WorkflowTemplate template) {
        Integer nodeCount = Math.toIntExact(nodeMapper.selectCount(new LambdaQueryWrapper<WorkflowTemplateNode>()
                .eq(WorkflowTemplateNode::getTemplateId, template.getId())
                .eq(WorkflowTemplateNode::getStatus, STATUS_ENABLED)));
        return WorkflowTemplateListVO.builder()
                .id(template.getId())
                .name(template.getName())
                .code(template.getCode())
                .description(template.getDescription())
                .sceneType(template.getSceneType())
                .coverUrl(template.getCoverUrl())
                .nodeCount(nodeCount)
                .sortOrder(template.getSortOrder())
                .status(template.getStatus())
                .createTime(template.getCreateTime())
                .updateTime(template.getUpdateTime())
                .build();
    }

    private WorkflowTemplateDetailVO toDetailVO(WorkflowTemplate template, boolean enabledNodesOnly) {
        return WorkflowTemplateDetailVO.builder()
                .id(template.getId())
                .name(template.getName())
                .code(template.getCode())
                .description(template.getDescription())
                .sceneType(template.getSceneType())
                .coverUrl(template.getCoverUrl())
                .sortOrder(template.getSortOrder())
                .status(template.getStatus())
                .nodes(listNodes(template.getId(), enabledNodesOnly).stream().map(this::toNodeVO).toList())
                .createTime(template.getCreateTime())
                .updateTime(template.getUpdateTime())
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

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
