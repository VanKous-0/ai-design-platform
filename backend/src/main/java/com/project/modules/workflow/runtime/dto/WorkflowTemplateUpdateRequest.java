package com.project.modules.workflow.runtime.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowTemplateUpdateRequest {

    @NotBlank(message = "Template name cannot be blank")
    private String name;

    @NotBlank(message = "Template code cannot be blank")
    private String code;

    private String description;

    private String sceneType;

    private String coverUrl;

    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;

    private Integer status;
}
