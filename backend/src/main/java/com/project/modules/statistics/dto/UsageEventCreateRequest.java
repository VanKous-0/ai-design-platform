package com.project.modules.statistics.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsageEventCreateRequest {

    @Size(max = 100, message = "Anonymous ID cannot exceed 100 characters")
    private String anonymousId;

    @NotBlank(message = "Event type cannot be blank")
    @Size(max = 50, message = "Event type cannot exceed 50 characters")
    private String eventType;

    @Size(max = 50, message = "Target type cannot exceed 50 characters")
    private String targetType;

    private Long targetId;

    @Size(max = 500, message = "Page URL cannot exceed 500 characters")
    private String pageUrl;

    @Min(value = 0, message = "Stay duration cannot be negative")
    private Integer stayDuration;

    @Size(max = 200, message = "Input summary cannot exceed 200 characters")
    private String inputSummary;

    @Size(max = 2000, message = "Extra JSON cannot exceed 2000 characters")
    private String extraJson;
}
