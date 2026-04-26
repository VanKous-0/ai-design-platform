package com.project.modules.profile.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    private String realName;

    private String school;

    private String major;

    private String grade;

    private String phone;

    private String bio;

    private String avatarUrl;
}
