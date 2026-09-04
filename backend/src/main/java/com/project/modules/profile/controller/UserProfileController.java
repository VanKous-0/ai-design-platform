package com.project.modules.profile.controller;

import com.project.common.result.Result;
import com.project.modules.profile.dto.UserDesignPreferenceUpdateRequest;
import com.project.modules.profile.dto.UserProfileUpdateRequest;
import com.project.modules.profile.dto.UserRecentParameterCreateRequest;
import com.project.modules.profile.dto.UserPreferenceSignalUpsertRequest;
import com.project.modules.profile.service.UserProfileService;
import com.project.modules.profile.vo.UserDesignPreferenceVO;
import com.project.modules.profile.vo.UserProfileVO;
import com.project.modules.profile.vo.UserRecentParameterVO;
import com.project.modules.profile.vo.UserPreferenceContextVO;
import com.project.modules.profile.vo.UserPreferenceSignalVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile(Authentication authentication) {
        return Result.success(userProfileService.getProfile(currentUserId(authentication)));
    }

    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        return Result.success(userProfileService.updateProfile(currentUserId(authentication), request));
    }

    @GetMapping("/preferences")
    public Result<UserDesignPreferenceVO> getPreference(Authentication authentication) {
        return Result.success(userProfileService.getPreference(currentUserId(authentication)));
    }

    @PutMapping("/preferences")
    public Result<UserDesignPreferenceVO> updatePreference(
            Authentication authentication,
            @Valid @RequestBody UserDesignPreferenceUpdateRequest request
    ) {
        return Result.success(userProfileService.updatePreference(currentUserId(authentication), request));
    }

    @PostMapping("/preference-signals")
    public Result<UserPreferenceSignalVO> upsertPreferenceSignal(
            Authentication authentication,
            @Valid @RequestBody UserPreferenceSignalUpsertRequest request
    ) {
        return Result.success(userProfileService.upsertPreferenceSignal(currentUserId(authentication), request));
    }

    @GetMapping("/preference-context")
    public Result<UserPreferenceContextVO> getPreferenceContext(Authentication authentication) {
        return Result.success(userProfileService.getPreferenceContext(currentUserId(authentication)));
    }

    @GetMapping("/recent-parameters")
    public Result<List<UserRecentParameterVO>> listRecentParameters(Authentication authentication) {
        return Result.success(userProfileService.listRecentParameters(currentUserId(authentication)));
    }

    @PostMapping("/recent-parameters")
    public Result<UserRecentParameterVO> saveRecentParameter(
            Authentication authentication,
            @Valid @RequestBody UserRecentParameterCreateRequest request
    ) {
        return Result.success(userProfileService.saveRecentParameter(currentUserId(authentication), request));
    }

    @DeleteMapping("/recent-parameters/{id}")
    public Result<Void> deleteRecentParameter(Authentication authentication, @PathVariable Long id) {
        userProfileService.deleteRecentParameter(currentUserId(authentication), id);
        return Result.success();
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
