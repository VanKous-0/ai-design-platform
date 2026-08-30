package com.project.modules.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    @Size(max = 50, message = "姓名不能超过50个字符")
    private String realName;

    @Size(max = 100, message = "学校不能超过100个字符")
    private String school;

    @Size(max = 100, message = "专业不能超过100个字符")
    private String major;

    @Size(max = 50, message = "年级不能超过50个字符")
    private String grade;

    @Size(max = 30, message = "手机号不能超过30个字符")
    private String phone;

    @Size(max = 500, message = "个人简介不能超过500个字符")
    private String bio;

    @Size(max = 500, message = "头像地址不能超过500个字符")
    private String avatarUrl;
}
