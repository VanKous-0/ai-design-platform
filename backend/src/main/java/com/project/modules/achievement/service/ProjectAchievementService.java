package com.project.modules.achievement.service;

import com.project.modules.achievement.dto.ProjectAchievementSaveRequest;
import com.project.modules.achievement.vo.ProjectAchievementVO;

import java.util.List;

public interface ProjectAchievementService {

    List<ProjectAchievementVO> listAchievements(String achievementType);

    ProjectAchievementVO getAchievement(Long id);

    ProjectAchievementVO createAchievement(ProjectAchievementSaveRequest request);

    ProjectAchievementVO updateAchievement(Long id, ProjectAchievementSaveRequest request);

    void deleteAchievement(Long id);
}
