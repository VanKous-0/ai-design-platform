package com.project.modules.caseproject.controller;

import com.project.common.result.Result;
import com.project.modules.caseproject.dto.UserCaseAssetCreateRequest;
import com.project.modules.caseproject.dto.UserCaseCreateRequest;
import com.project.modules.caseproject.dto.UserCaseUpdateRequest;
import com.project.modules.caseproject.service.CaseProjectService;
import com.project.modules.caseproject.vo.CaseAssetVO;
import com.project.modules.caseproject.vo.UserCaseDetailVO;
import com.project.modules.caseproject.vo.UserCaseVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/user")
public class UserCaseController {

    private final CaseProjectService caseProjectService;

    public UserCaseController(CaseProjectService caseProjectService) {
        this.caseProjectService = caseProjectService;
    }

    @PostMapping("/cases")
    public Result<UserCaseDetailVO> createCase(
            Authentication authentication,
            @Valid @RequestBody UserCaseCreateRequest request
    ) {
        return Result.success(caseProjectService.createUserCase(currentUserId(authentication), request));
    }

    @GetMapping("/cases/my")
    public Result<?> listMyCases(
            Authentication authentication,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize
    ) {
        if (pageNum != null || pageSize != null) {
            return Result.success(caseProjectService.pageMyCases(
                    currentUserId(authentication),
                    pageNum,
                    pageSize
            ));
        }
        return Result.success(caseProjectService.listMyCases(currentUserId(authentication)));
    }

    @GetMapping("/cases/{id}")
    public Result<UserCaseDetailVO> getMyCase(Authentication authentication, @PathVariable Long id) {
        return Result.success(caseProjectService.getMyCaseDetail(currentUserId(authentication), id));
    }

    @PutMapping("/cases/{id}")
    public Result<UserCaseDetailVO> updateMyCase(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UserCaseUpdateRequest request
    ) {
        return Result.success(caseProjectService.updateMyCase(currentUserId(authentication), id, request));
    }

    @PostMapping("/cases/{id}/assets")
    public Result<CaseAssetVO> createMyCaseAsset(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UserCaseAssetCreateRequest request
    ) {
        return Result.success(caseProjectService.createMyCaseAsset(currentUserId(authentication), id, request));
    }

    @DeleteMapping("/case-assets/{id}")
    public Result<Void> deleteMyCaseAsset(Authentication authentication, @PathVariable Long id) {
        caseProjectService.deleteMyCaseAsset(currentUserId(authentication), id);
        return Result.success();
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
