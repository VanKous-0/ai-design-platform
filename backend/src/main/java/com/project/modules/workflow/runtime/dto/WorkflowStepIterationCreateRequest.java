package com.project.modules.workflow.runtime.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkflowStepIterationCreateRequest {

    private Long toolId;

    private Long promptId;

    private Long promptRevisionId;

    @Size(max = 10000, message = "Prompt content cannot exceed 10000 characters")
    private String promptContent;

    /**
     * @deprecated Retained for request compatibility. The server ignores this value and builds
     * the persisted snapshot from the authenticated user's effective profile.
     */
    @Deprecated
    private String profileContextSnapshot;

    @Size(max = 20000, message = "Output content cannot exceed 20000 characters")
    private String outputContent;

    @Size(max = 500, message = "Result URL cannot exceed 500 characters")
    private String resultUrl;

    @Min(value = 1, message = "Effect score must be between 1 and 10")
    @Max(value = 10, message = "Effect score must be between 1 and 10")
    private Integer effectScore;

    @Min(value = 1, message = "Accuracy score must be between 1 and 10")
    @Max(value = 10, message = "Accuracy score must be between 1 and 10")
    private Integer accuracyScore;

    @Min(value = 1, message = "Controllability score must be between 1 and 10")
    @Max(value = 10, message = "Controllability score must be between 1 and 10")
    private Integer controllabilityScore;

    @Min(value = 1, message = "Usability score must be between 1 and 10")
    @Max(value = 10, message = "Usability score must be between 1 and 10")
    private Integer usabilityScore;

    @Size(max = 2000, message = "Improvement note cannot exceed 2000 characters")
    private String improvementNote;

    private Boolean selected;
}
