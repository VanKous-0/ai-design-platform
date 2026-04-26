package com.project.modules.workflow.runtime.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkflowTemplateNodeCreateRequest {

    @NotNull(message = "Template ID cannot be null")
    private Long templateId;

    private Long stageId;

    private Long stepId;

    @NotBlank(message = "Node name cannot be blank")
    private String nodeName;

    @NotBlank(message = "Node code cannot be blank")
    private String nodeCode;

    private String nodeType;

    private String inputDesc;

    private String outputDesc;

    private String nextTip;

    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;

    private Integer status;
}
