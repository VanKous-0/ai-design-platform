package com.project.modules.workflow.runtime.service;

import com.project.modules.workflow.runtime.dto.WorkflowNodePromptSetRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateNodeCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateNodeUpdateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateUpdateRequest;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateListVO;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateNodeVO;

import java.util.List;

public interface WorkflowTemplateService {

    List<WorkflowTemplateListVO> listEnabledTemplates();

    WorkflowTemplateDetailVO getEnabledTemplate(Long id);

    WorkflowTemplateDetailVO createTemplate(WorkflowTemplateCreateRequest request);

    WorkflowTemplateDetailVO updateTemplate(Long id, WorkflowTemplateUpdateRequest request);

    void deleteTemplate(Long id);

    WorkflowTemplateNodeVO createNode(WorkflowTemplateNodeCreateRequest request);

    WorkflowTemplateNodeVO updateNode(Long id, WorkflowTemplateNodeUpdateRequest request);

    void deleteNode(Long id);

    List<Long> setNodePrompts(Long nodeId, WorkflowNodePromptSetRequest request);
}
