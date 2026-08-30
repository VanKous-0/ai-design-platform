package com.project.modules.achievement.controller;

import com.project.common.result.Result;
import com.project.modules.achievement.dto.ProjectAchievementSaveRequest;
import com.project.modules.achievement.service.ProjectAchievementService;
import com.project.modules.achievement.vo.ProjectAchievementVO;
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
@RequestMapping("/api/admin/achievements")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProjectAchievementController {

    private final ProjectAchievementService service;

    public AdminProjectAchievementController(ProjectAchievementService service) {
        this.service = service;
    }

    @PostMapping
    public Result<ProjectAchievementVO> create(
            @Valid @RequestBody ProjectAchievementSaveRequest request
    ) {
        return Result.success(service.createAchievement(request));
    }

    @PutMapping("/{id}")
    public Result<ProjectAchievementVO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectAchievementSaveRequest request
    ) {
        return Result.success(service.updateAchievement(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.deleteAchievement(id);
        return Result.success();
    }
}
