package com.project.modules.profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRecentParameterCreateRequest {

    @NotBlank(message = "Parameter type cannot be blank")
    private String parameterType;

    @NotBlank(message = "Parameter key cannot be blank")
    private String parameterKey;

    @NotBlank(message = "Parameter value cannot be blank")
    private String parameterValue;

    private String source;
}
