package com.project.modules.profile.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileVO {

    private Long id;

    private Long userId;

    private String realName;

    private String school;

    private String major;

    private String grade;

    private String phone;

    private String bio;

    private String avatarUrl;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
