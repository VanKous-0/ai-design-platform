package com.project.modules.workflow.runtime.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkflowInstanceCreateRequest {

    @NotNull(message = "Template ID cannot be null")
    private Long templateId;

    private String title;
}
