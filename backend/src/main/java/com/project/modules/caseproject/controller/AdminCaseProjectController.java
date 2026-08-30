package com.project.modules.caseproject.controller;

import com.project.common.result.Result;
import com.project.modules.caseproject.dto.CaseAssetCreateRequest;
import com.project.modules.caseproject.dto.CaseAssetUpdateRequest;
import com.project.modules.caseproject.dto.CaseAuditRequest;
import com.project.modules.caseproject.dto.CaseProjectCreateRequest;
import com.project.modules.caseproject.dto.CaseProjectUpdateRequest;
import com.project.modules.caseproject.service.CaseProjectService;
import com.project.modules.caseproject.vo.CaseAssetVO;
import com.project.modules.caseproject.vo.CaseAuditVO;
import com.project.modules.caseproject.vo.CaseProjectDetailVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCaseProjectController {

    private final CaseProjectService caseProjectService;

    public AdminCaseProjectController(CaseProjectService caseProjectService) {
        this.caseProjectService = caseProjectService;
    }

    @PostMapping("/cases")
    public Result<CaseProjectDetailVO> createCase(@Valid @RequestBody CaseProjectCreateRequest request) {
        return Result.success(caseProjectService.createCase(request));
    }

    @PutMapping("/cases/{id}")
    public Result<CaseProjectDetailVO> updateCase(
            @PathVariable Long id,
            @Valid @RequestBody CaseProjectUpdateRequest request
    ) {
        return Result.success(caseProjectService.updateCase(id, request));
    }

    @DeleteMapping("/cases/{id}")
    public Result<Void> deleteCase(@PathVariable Long id) {
        caseProjectService.deleteCase(id);
        return Result.success();
    }

    @PostMapping("/cases/{id}/assets")
    public Result<CaseAssetVO> createAsset(
            @PathVariable Long id,
            @Valid @RequestBody CaseAssetCreateRequest request
    ) {
        return Result.success(caseProjectService.createAsset(id, request));
    }

    @PutMapping("/case-assets/{id}")
    public Result<CaseAssetVO> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody CaseAssetUpdateRequest request
    ) {
        return Result.success(caseProjectService.updateAsset(id, request));
    }

    @DeleteMapping("/case-assets/{id}")
    public Result<Void> deleteAsset(@PathVariable Long id) {
        caseProjectService.deleteAsset(id);
        return Result.success();
    }

    @GetMapping("/cases/pending")
    public Result<List<CaseAuditVO>> listPendingCases() {
        return Result.success(caseProjectService.listPendingCases());
    }

    @GetMapping("/cases/audit-list")
    public Result<?> listAuditCases(
            @RequestParam(required = false) String auditStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long submitUserId,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize
    ) {
        if (pageNum != null || pageSize != null) {
            return Result.success(caseProjectService.pageAuditCases(
                    auditStatus,
                    keyword,
                    submitUserId,
                    pageNum,
                    pageSize
            ));
        }
        return Result.success(caseProjectService.listAuditCases(auditStatus, keyword, submitUserId));
    }

    @PostMapping("/cases/{id}/approve")
    public Result<CaseAuditVO> approveCase(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CaseAuditRequest request
    ) {
        return Result.success(caseProjectService.approveCase(currentUserId(authentication), id, request));
    }

    @PostMapping("/cases/{id}/reject")
    public Result<CaseAuditVO> rejectCase(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CaseAuditRequest request
    ) {
        return Result.success(caseProjectService.rejectCase(currentUserId(authentication), id, request));
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
