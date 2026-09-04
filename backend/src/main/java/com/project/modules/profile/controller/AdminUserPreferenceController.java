package com.project.modules.profile.controller;

import com.project.common.result.Result;
import com.project.modules.profile.dto.UserPreferenceSignalUpsertRequest;
import com.project.modules.profile.service.UserPreferenceSignalService;
import com.project.modules.profile.vo.UserPreferenceSignalVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users/{userId}/preference-signals")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserPreferenceController {

    private final UserPreferenceSignalService preferenceSignalService;

    public AdminUserPreferenceController(UserPreferenceSignalService preferenceSignalService) {
        this.preferenceSignalService = preferenceSignalService;
    }

    @PostMapping
    public Result<UserPreferenceSignalVO> upsertInferredPreference(
            @PathVariable Long userId,
            @Valid @RequestBody UserPreferenceSignalUpsertRequest request
    ) {
        return Result.success(preferenceSignalService.upsertInferred(userId, request));
    }
}
