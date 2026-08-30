package com.project.modules.prompt.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromptParameterCreateRequest {

    @NotBlank(message = "Parameter key cannot be blank")
    private String paramKey;

    @NotBlank(message = "Parameter name cannot be blank")
    private String paramName;

    private String paramType;

    private Boolean required;

    private String defaultValue;

    private String placeholder;

    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;
}
