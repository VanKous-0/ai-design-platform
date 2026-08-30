package com.project.modules.achievement.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectAchievementVO {

    private Long id;
    private String code;
    private String achievementType;
    private String title;
    private String projectName;
    private String competitionName;
    private String issuer;
    private String awardLevel;
    private LocalDate achievementDate;
    private String participants;
    private String summary;
    private String evidenceUrl;
    private String sourceFile;
    private String sourceDesc;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
