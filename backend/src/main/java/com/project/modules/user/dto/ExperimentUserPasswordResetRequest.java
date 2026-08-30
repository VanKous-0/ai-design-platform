package com.project.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExperimentUserPasswordResetRequest {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 72, message = "新密码长度必须在8到72个字符之间")
    private String newPassword;
}
