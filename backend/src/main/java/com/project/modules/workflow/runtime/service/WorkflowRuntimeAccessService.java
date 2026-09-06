package com.project.modules.workflow.runtime.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.common.exception.DomainError;
import com.project.common.exception.DomainException;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.mapper.AiToolMapper;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import com.project.modules.workflow.runtime.entity.WorkflowTemplate;
import com.project.modules.workflow.runtime.entity.WorkflowTemplateNode;
import com.project.modules.workflow.runtime.mapper.WorkflowInstanceMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateNodeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowRuntimeAccessService {

    private static final int STATUS_ENABLED = 1;

    private final WorkflowTemplateMapper templateMapper;
    private final WorkflowTemplateNodeMapper nodeMapper;
    private final WorkflowInstanceMapper instanceMapper;
    private final AiToolMapper aiToolMapper;

    public WorkflowRuntimeAccessService(
            WorkflowTemplateMapper templateMapper,
            WorkflowTemplateNodeMapper nodeMapper,
            WorkflowInstanceMapper instanceMapper,
            AiToolMapper aiToolMapper
    ) {
        this.templateMapper = templateMapper;
        this.nodeMapper = nodeMapper;
        this.instanceMapper = instanceMapper;
        this.aiToolMapper = aiToolMapper;
    }

    public WorkflowTemplate requireEnabledTemplate(Long templateId) {
        WorkflowTemplate template = templateMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplate>()
                .eq(WorkflowTemplate::getId, templateId)
                .eq(WorkflowTemplate::getStatus, STATUS_ENABLED)
                .last("limit 1"));
        if (template == null) {
            throw new DomainException(DomainError.WORKFLOW_NOT_FOUND,
                    "Workflow template does not exist or is disabled");
        }
        return template;
    }

    public WorkflowInstance requireOwnedInstance(Long userId, Long instanceId) {
        WorkflowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new DomainException(DomainError.WORKFLOW_NOT_FOUND, "Workflow instance does not exist");
        }
        if (!instance.getUserId().equals(userId)) {
            throw new DomainException(DomainError.WORKFLOW_NOT_OWNED,
                    "Workflow instance belongs to another user");
        }
        return instance;
    }

    public List<WorkflowTemplateNode> listEnabledNodes(Long templateId) {
        return nodeMapper.selectList(new LambdaQueryWrapper<WorkflowTemplateNode>()
                .eq(WorkflowTemplateNode::getTemplateId, templateId)
                .eq(WorkflowTemplateNode::getStatus, STATUS_ENABLED)
                .orderByAsc(WorkflowTemplateNode::getSortOrder)
                .orderByAsc(WorkflowTemplateNode::getId));
    }

    public WorkflowTemplateNode requireEnabledNode(Long templateId, Long nodeId) {
        WorkflowTemplateNode node = nodeMapper.selectOne(new LambdaQueryWrapper<WorkflowTemplateNode>()
                .eq(WorkflowTemplateNode::getId, nodeId)
                .eq(WorkflowTemplateNode::getTemplateId, templateId)
                .eq(WorkflowTemplateNode::getStatus, STATUS_ENABLED)
                .last("limit 1"));
        if (node == null) {
            throw new DomainException(DomainError.WORKFLOW_NOT_FOUND,
                    "Workflow node does not belong to this template or is disabled");
        }
        return node;
    }

    public AiTool requireEnabledTool(Long toolId) {
        if (toolId == null) {
            return null;
        }
        AiTool tool = aiToolMapper.selectOne(new LambdaQueryWrapper<AiTool>()
                .eq(AiTool::getId, toolId)
                .eq(AiTool::getStatus, STATUS_ENABLED)
                .last("limit 1"));
        if (tool == null) {
            throw new DomainException(DomainError.WORKFLOW_NOT_FOUND,
                    "AI tool does not exist or is disabled");
        }
        return tool;
    }
}
