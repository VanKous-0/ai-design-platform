package com.project.modules.caseproject.controller;

import com.project.common.result.Result;
import com.project.modules.caseproject.dto.CaseAssetCreateRequest;
import com.project.modules.caseproject.dto.CaseAssetUpdateRequest;
import com.project.modules.caseproject.dto.CaseProjectCreateRequest;
import com.project.modules.caseproject.dto.CaseProjectUpdateRequest;
import com.project.modules.caseproject.service.CaseProjectService;
import com.project.modules.caseproject.vo.CaseAssetVO;
import com.project.modules.caseproject.vo.CaseProjectDetailVO;
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
}
