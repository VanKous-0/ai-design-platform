package com.project.modules.statistics.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SurveyFeedbackCreateRequest {

    @Size(max = 100, message = "Anonymous ID cannot exceed 100 characters")
    private String anonymousId;

    @NotBlank(message = "Scene cannot be blank")
    @Size(max = 100, message = "Scene cannot exceed 100 characters")
    private String scene;

    @NotNull(message = "Score cannot be null")
    @DecimalMin(value = "0.0", message = "Score cannot be less than 0")
    @DecimalMax(value = "10.0", message = "Score cannot be greater than 10")
    private BigDecimal score;

    private String content;

    @Size(max = 100, message = "Contact cannot exceed 100 characters")
    private String contact;
}
