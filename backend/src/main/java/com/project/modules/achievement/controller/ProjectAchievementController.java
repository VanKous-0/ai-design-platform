package com.project.modules.achievement.controller;

import com.project.common.result.Result;
import com.project.modules.achievement.service.ProjectAchievementService;
import com.project.modules.achievement.vo.ProjectAchievementVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
public class ProjectAchievementController {

    private final ProjectAchievementService service;

    public ProjectAchievementController(ProjectAchievementService service) {
        this.service = service;
    }

    @GetMapping
    public Result<List<ProjectAchievementVO>> list(
            @RequestParam(required = false) String achievementType
    ) {
        return Result.success(service.listAchievements(achievementType));
    }

    @GetMapping("/{id}")
    public Result<ProjectAchievementVO> detail(@PathVariable Long id) {
        return Result.success(service.getAchievement(id));
    }
}
