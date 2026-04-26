package com.project.modules.workflow.runtime.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class WorkflowStepCompleteRequest {

    private String inputContent;

    private String outputContent;

    @Min(value = 0, message = "Duration cannot be negative")
    private Integer durationSeconds;
}
