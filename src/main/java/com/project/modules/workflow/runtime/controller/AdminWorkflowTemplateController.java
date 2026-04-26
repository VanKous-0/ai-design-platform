package com.project.modules.workflow.runtime.controller;

import com.project.common.result.Result;
import com.project.modules.workflow.runtime.dto.WorkflowNodePromptSetRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateNodeCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateNodeUpdateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowTemplateUpdateRequest;
import com.project.modules.workflow.runtime.service.WorkflowTemplateService;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowTemplateNodeVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWorkflowTemplateController {

    private final WorkflowTemplateService workflowTemplateService;

    public AdminWorkflowTemplateController(WorkflowTemplateService workflowTemplateService) {
        this.workflowTemplateService = workflowTemplateService;
    }

    @PostMapping("/workflow-templates")
    public Result<WorkflowTemplateDetailVO> createTemplate(@Valid @RequestBody WorkflowTemplateCreateRequest request) {
        return Result.success(workflowTemplateService.createTemplate(request));
    }

    @PutMapping("/workflow-templates/{id}")
    public Result<WorkflowTemplateDetailVO> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowTemplateUpdateRequest request
    ) {
        return Result.success(workflowTemplateService.updateTemplate(id, request));
    }

    @DeleteMapping("/workflow-templates/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        workflowTemplateService.deleteTemplate(id);
        return Result.success();
    }

    @PostMapping("/workflow-template-nodes")
    public Result<WorkflowTemplateNodeVO> createNode(@Valid @RequestBody WorkflowTemplateNodeCreateRequest request) {
        return Result.success(workflowTemplateService.createNode(request));
    }

    @PutMapping("/workflow-template-nodes/{id}")
    public Result<WorkflowTemplateNodeVO> updateNode(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowTemplateNodeUpdateRequest request
    ) {
        return Result.success(workflowTemplateService.updateNode(id, request));
    }

    @DeleteMapping("/workflow-template-nodes/{id}")
    public Result<Void> deleteNode(@PathVariable Long id) {
        workflowTemplateService.deleteNode(id);
        return Result.success();
    }

    @PostMapping("/workflow-template-nodes/{nodeId}/prompts")
    public Result<List<Long>> setNodePrompts(
            @PathVariable Long nodeId,
            @Valid @RequestBody WorkflowNodePromptSetRequest request
    ) {
        return Result.success(workflowTemplateService.setNodePrompts(nodeId, request));
    }
}
