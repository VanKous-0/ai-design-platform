package com.project.modules.workflow.runtime.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class WorkflowNodePromptSetRequest {

    @NotNull(message = "Prompt IDs cannot be null")
    private List<Long> promptIds;
}
