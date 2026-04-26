package com.project.modules.statistics.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsageEventCreateRequest {

    private String anonymousId;

    @NotBlank(message = "Event type cannot be blank")
    private String eventType;

    private String targetType;

    private Long targetId;

    private String pageUrl;

    @Min(value = 0, message = "Stay duration cannot be negative")
    private Integer stayDuration;

    private String inputSummary;

    private String extraJson;
}
