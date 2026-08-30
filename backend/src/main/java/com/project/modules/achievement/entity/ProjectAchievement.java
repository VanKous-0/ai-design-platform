package com.project.modules.achievement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("project_achievement")
public class ProjectAchievement {

    @TableId(type = IdType.AUTO)
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

    @TableLogic
    private Integer isDeleted;
}
