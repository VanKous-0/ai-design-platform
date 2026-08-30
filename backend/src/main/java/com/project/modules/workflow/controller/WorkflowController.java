package com.project.modules.workflow.controller;

import com.project.common.result.Result;
import com.project.modules.workflow.service.WorkflowService;
import com.project.modules.workflow.vo.WorkflowStageVO;
import com.project.modules.workflow.vo.WorkflowStepVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stages")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public Result<List<WorkflowStageVO>> listStages() {
        return Result.success(workflowService.listEnabledStages());
    }

    @GetMapping("/{id}")
    public Result<WorkflowStageVO> getStage(@PathVariable Long id) {
        return Result.success(workflowService.getEnabledStage(id));
    }

    @GetMapping("/{id}/steps")
    public Result<List<WorkflowStepVO>> listSteps(@PathVariable Long id) {
        return Result.success(workflowService.listEnabledStepsByStageId(id));
    }
}
