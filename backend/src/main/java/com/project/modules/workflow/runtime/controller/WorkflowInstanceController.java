package com.project.modules.workflow.runtime.controller;

import com.project.common.result.Result;
import com.project.modules.workflow.runtime.dto.WorkflowInstanceCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepCompleteRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepIterationCreateRequest;
import com.project.modules.workflow.runtime.service.WorkflowInstanceService;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceListVO;
import com.project.modules.workflow.runtime.vo.WorkflowNextStepVO;
import com.project.modules.workflow.runtime.vo.WorkflowProgressVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepCompleteVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepIterationVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workflow-instances")
public class WorkflowInstanceController {

    private final WorkflowInstanceService workflowInstanceService;

    public WorkflowInstanceController(WorkflowInstanceService workflowInstanceService) {
        this.workflowInstanceService = workflowInstanceService;
    }

    @PostMapping
    public Result<WorkflowInstanceDetailVO> createInstance(
            Authentication authentication,
            @Valid @RequestBody WorkflowInstanceCreateRequest request
    ) {
        return Result.success(workflowInstanceService.createInstance(currentUserId(authentication), request));
    }

    @GetMapping("/my")
    public Result<?> listMyInstances(
            Authentication authentication,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize
    ) {
        if (pageNum != null || pageSize != null) {
            return Result.success(workflowInstanceService.pageMyInstances(
                    currentUserId(authentication),
                    pageNum,
                    pageSize
            ));
        }
        return Result.success(workflowInstanceService.listMyInstances(currentUserId(authentication)));
    }

    @GetMapping("/{id}")
    public Result<WorkflowInstanceDetailVO> getInstance(Authentication authentication, @PathVariable Long id) {
        return Result.success(workflowInstanceService.getMyInstance(currentUserId(authentication), id));
    }

    @GetMapping("/{id}/progress")
    public Result<WorkflowProgressVO> getProgress(Authentication authentication, @PathVariable Long id) {
        return Result.success(workflowInstanceService.getProgress(currentUserId(authentication), id));
    }

    @GetMapping("/{id}/next-step")
    public Result<WorkflowNextStepVO> getNextStep(Authentication authentication, @PathVariable Long id) {
        return Result.success(workflowInstanceService.getNextStep(currentUserId(authentication), id));
    }

    @PostMapping("/{id}/steps/{nodeId}/complete")
    public Result<WorkflowStepCompleteVO> completeStep(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Long nodeId,
            @Valid @RequestBody WorkflowStepCompleteRequest request
    ) {
        return Result.success(workflowInstanceService.completeStep(currentUserId(authentication), id, nodeId, request));
    }

    @PostMapping("/{id}/steps/{nodeId}/iterations")
    public Result<WorkflowStepIterationVO> createStepIteration(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Long nodeId,
            @Valid @RequestBody WorkflowStepIterationCreateRequest request
    ) {
        return Result.success(workflowInstanceService.createStepIteration(
                currentUserId(authentication),
                id,
                nodeId,
                request
        ));
    }

    @GetMapping("/{id}/steps/{nodeId}/iterations")
    public Result<List<WorkflowStepIterationVO>> listStepIterations(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Long nodeId
    ) {
        return Result.success(workflowInstanceService.listStepIterations(
                currentUserId(authentication),
                id,
                nodeId
        ));
    }

    @PutMapping("/{id}/steps/{nodeId}/iterations/{iterationId}/select")
    public Result<WorkflowStepIterationVO> selectStepIteration(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Long nodeId,
            @PathVariable Long iterationId
    ) {
        return Result.success(workflowInstanceService.selectStepIteration(
                currentUserId(authentication),
                id,
                nodeId,
                iterationId
        ));
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
