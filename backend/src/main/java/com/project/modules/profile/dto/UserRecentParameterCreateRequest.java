package com.project.modules.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRecentParameterCreateRequest {

    @NotBlank(message = "Parameter type cannot be blank")
    @Size(max = 50, message = "Parameter type cannot exceed 50 characters")
    private String parameterType;

    @NotBlank(message = "Parameter key cannot be blank")
    @Size(max = 100, message = "Parameter key cannot exceed 100 characters")
    private String parameterKey;

    @NotBlank(message = "Parameter value cannot be blank")
    @Size(max = 500, message = "Parameter value cannot exceed 500 characters")
    private String parameterValue;

    @Size(max = 50, message = "Source cannot exceed 50 characters")
    private String source;
}
