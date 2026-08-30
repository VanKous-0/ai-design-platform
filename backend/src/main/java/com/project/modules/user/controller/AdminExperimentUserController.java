package com.project.modules.user.controller;

import com.project.common.result.PageResult;
import com.project.common.result.Result;
import com.project.modules.user.dto.ExperimentUserBatchCreateRequest;
import com.project.modules.user.dto.ExperimentUserPasswordResetRequest;
import com.project.modules.user.dto.ExperimentUserStatusUpdateRequest;
import com.project.modules.user.service.ExperimentUserService;
import com.project.modules.user.vo.ExperimentUserCredentialVO;
import com.project.modules.user.vo.ExperimentUserVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/admin/experiment-users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminExperimentUserController {

    private final ExperimentUserService experimentUserService;

    public AdminExperimentUserController(ExperimentUserService experimentUserService) {
        this.experimentUserService = experimentUserService;
    }

    @PostMapping("/batch")
    public Result<List<ExperimentUserCredentialVO>> createBatch(
            @Valid @RequestBody ExperimentUserBatchCreateRequest request
    ) {
        return Result.success(experimentUserService.createBatch(request));
    }

    @GetMapping
    public Result<PageResult<ExperimentUserVO>> pageUsers(
            @RequestParam(required = false) String experimentBatch,
            @RequestParam(required = false) String experimentGroup,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize
    ) {
        return Result.success(experimentUserService.pageUsers(
                experimentBatch,
                experimentGroup,
                keyword,
                pageNum,
                pageSize
        ));
    }

    @PutMapping("/{id}/status")
    public Result<ExperimentUserVO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ExperimentUserStatusUpdateRequest request
    ) {
        return Result.success(experimentUserService.updateStatus(id, request.getStatus()));
    }

    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ExperimentUserPasswordResetRequest request
    ) {
        experimentUserService.resetPassword(id, request.getNewPassword());
        return Result.success();
    }
}
