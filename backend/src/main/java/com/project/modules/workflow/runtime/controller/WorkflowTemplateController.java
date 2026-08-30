package com.project.modules.workflow.runtime.controller;

import com.project.common.result.Result;
import com.project.modules.workflow.runtime.service.WorkflowTemplateService;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workflow-templates")
public class WorkflowTemplateController {

    private final WorkflowTemplateService workflowTemplateService;

    public WorkflowTemplateController(WorkflowTemplateService workflowTemplateService) {
        this.workflowTemplateService = workflowTemplateService;
    }

    @GetMapping
    public Result<List<WorkflowTemplateListVO>> listTemplates() {
        return Result.success(workflowTemplateService.listEnabledTemplates());
    }

    @GetMapping("/{id}")
    public Result<WorkflowTemplateDetailVO> getTemplate(@PathVariable Long id) {
        return Result.success(workflowTemplateService.getEnabledTemplate(id));
    }
}
