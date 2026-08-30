package com.project.modules.achievement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectAchievementSaveRequest {

    @NotBlank(message = "成果编码不能为空")
    @Size(max = 80, message = "成果编码不能超过80个字符")
    private String code;

    @NotBlank(message = "成果类型不能为空")
    private String achievementType;

    @NotBlank(message = "成果标题不能为空")
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

    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;

    private Integer status;
}
