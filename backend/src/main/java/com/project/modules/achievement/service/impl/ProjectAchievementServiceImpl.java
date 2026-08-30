package com.project.modules.achievement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.common.exception.BusinessException;
import com.project.modules.achievement.dto.ProjectAchievementSaveRequest;
import com.project.modules.achievement.entity.ProjectAchievement;
import com.project.modules.achievement.mapper.ProjectAchievementMapper;
import com.project.modules.achievement.service.ProjectAchievementService;
import com.project.modules.achievement.vo.ProjectAchievementVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ProjectAchievementServiceImpl implements ProjectAchievementService {

    private static final int STATUS_ENABLED = 1;
    private static final Set<String> TYPES = Set.of(
            "AWARD",
            "COMPETITION_ENTRY",
            "DESIGN_WORK",
            "BUSINESS_PLAN"
    );

    private final ProjectAchievementMapper mapper;

    public ProjectAchievementServiceImpl(ProjectAchievementMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ProjectAchievementVO> listAchievements(String achievementType) {
        LambdaQueryWrapper<ProjectAchievement> query = new LambdaQueryWrapper<ProjectAchievement>()
                .eq(ProjectAchievement::getStatus, STATUS_ENABLED)
                .orderByAsc(ProjectAchievement::getSortOrder)
                .orderByAsc(ProjectAchievement::getId);
        if (StringUtils.hasText(achievementType)) {
            query.eq(ProjectAchievement::getAchievementType, normalizeType(achievementType));
        }
        return mapper.selectList(query).stream().map(this::toVO).toList();
    }

    @Override
    public ProjectAchievementVO getAchievement(Long id) {
        ProjectAchievement achievement = mapper.selectOne(
                new LambdaQueryWrapper<ProjectAchievement>()
                        .eq(ProjectAchievement::getId, id)
                        .eq(ProjectAchievement::getStatus, STATUS_ENABLED)
                        .last("limit 1")
        );
        if (achievement == null) {
            throw new BusinessException("成果不存在或未启用");
        }
        return toVO(achievement);
    }

    @Override
    public ProjectAchievementVO createAchievement(ProjectAchievementSaveRequest request) {
        ensureCodeUnique(request.getCode(), null);
        ProjectAchievement achievement = new ProjectAchievement();
        fill(achievement, request);
        LocalDateTime now = LocalDateTime.now();
        achievement.setCreateTime(now);
        achievement.setUpdateTime(now);
        achievement.setIsDeleted(0);
        mapper.insert(achievement);
        return toVO(achievement);
    }

    @Override
    public ProjectAchievementVO updateAchievement(Long id, ProjectAchievementSaveRequest request) {
        ProjectAchievement achievement = getEntity(id);
        ensureCodeUnique(request.getCode(), id);
        fill(achievement, request);
        achievement.setUpdateTime(LocalDateTime.now());
        mapper.updateById(achievement);
        return toVO(achievement);
    }

    @Override
    public void deleteAchievement(Long id) {
        mapper.deleteById(getEntity(id));
    }

    private ProjectAchievement getEntity(Long id) {
        ProjectAchievement achievement = mapper.selectById(id);
        if (achievement == null) {
            throw new BusinessException("成果不存在");
        }
        return achievement;
    }

    private void ensureCodeUnique(String code, Long excludeId) {
        ProjectAchievement existing = mapper.selectOne(
                new LambdaQueryWrapper<ProjectAchievement>()
                        .eq(ProjectAchievement::getCode, code)
                        .last("limit 1")
        );
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new BusinessException("成果编码已存在");
        }
    }

    private void fill(ProjectAchievement achievement, ProjectAchievementSaveRequest request) {
        achievement.setCode(request.getCode().trim());
        achievement.setAchievementType(normalizeType(request.getAchievementType()));
        achievement.setTitle(request.getTitle());
        achievement.setProjectName(request.getProjectName());
        achievement.setCompetitionName(request.getCompetitionName());
        achievement.setIssuer(request.getIssuer());
        achievement.setAwardLevel(request.getAwardLevel());
        achievement.setAchievementDate(request.getAchievementDate());
        achievement.setParticipants(request.getParticipants());
        achievement.setSummary(request.getSummary());
        achievement.setEvidenceUrl(request.getEvidenceUrl());
        achievement.setSourceFile(request.getSourceFile());
        achievement.setSourceDesc(request.getSourceDesc());
        achievement.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        achievement.setStatus(request.getStatus() == null ? STATUS_ENABLED : request.getStatus());
    }

    private String normalizeType(String value) {
        String type = value.trim().toUpperCase();
        if (!TYPES.contains(type)) {
            throw new BusinessException("成果类型不受支持");
        }
        return type;
    }

    private ProjectAchievementVO toVO(ProjectAchievement achievement) {
        return ProjectAchievementVO.builder()
                .id(achievement.getId())
                .code(achievement.getCode())
                .achievementType(achievement.getAchievementType())
                .title(achievement.getTitle())
                .projectName(achievement.getProjectName())
                .competitionName(achievement.getCompetitionName())
                .issuer(achievement.getIssuer())
                .awardLevel(achievement.getAwardLevel())
                .achievementDate(achievement.getAchievementDate())
                .participants(achievement.getParticipants())
                .summary(achievement.getSummary())
                .evidenceUrl(achievement.getEvidenceUrl())
                .sourceFile(achievement.getSourceFile())
                .sourceDesc(achievement.getSourceDesc())
                .sortOrder(achievement.getSortOrder())
                .status(achievement.getStatus())
                .createTime(achievement.getCreateTime())
                .updateTime(achievement.getUpdateTime())
                .build();
    }
}
