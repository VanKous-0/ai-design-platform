package com.project.modules.workflow.controller;

import com.project.common.result.Result;
import com.project.modules.workflow.dto.WorkflowStageCreateRequest;
import com.project.modules.workflow.dto.WorkflowStageUpdateRequest;
import com.project.modules.workflow.dto.WorkflowStepCreateRequest;
import com.project.modules.workflow.dto.WorkflowStepUpdateRequest;
import com.project.modules.workflow.service.WorkflowService;
import com.project.modules.workflow.vo.WorkflowStageVO;
import com.project.modules.workflow.vo.WorkflowStepVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWorkflowController {

    private final WorkflowService workflowService;

    public AdminWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/stages")
    public Result<WorkflowStageVO> createStage(@Valid @RequestBody WorkflowStageCreateRequest request) {
        return Result.success(workflowService.createStage(request));
    }

    @PutMapping("/stages/{id}")
    public Result<WorkflowStageVO> updateStage(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowStageUpdateRequest request
    ) {
        return Result.success(workflowService.updateStage(id, request));
    }

    @DeleteMapping("/stages/{id}")
    public Result<Void> deleteStage(@PathVariable Long id) {
        workflowService.deleteStage(id);
        return Result.success();
    }

    @PostMapping("/steps")
    public Result<WorkflowStepVO> createStep(@Valid @RequestBody WorkflowStepCreateRequest request) {
        return Result.success(workflowService.createStep(request));
    }

    @PutMapping("/steps/{id}")
    public Result<WorkflowStepVO> updateStep(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowStepUpdateRequest request
    ) {
        return Result.success(workflowService.updateStep(id, request));
    }

    @DeleteMapping("/steps/{id}")
    public Result<Void> deleteStep(@PathVariable Long id) {
        workflowService.deleteStep(id);
        return Result.success();
    }
}
