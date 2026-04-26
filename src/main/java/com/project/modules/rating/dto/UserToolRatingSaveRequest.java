package com.project.modules.rating.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserToolRatingSaveRequest {

    @NotNull(message = "Tool ID cannot be null")
    private Long toolId;

    @NotNull(message = "Effect score cannot be null")
    @DecimalMin(value = "0.0", message = "Effect score cannot be less than 0")
    @DecimalMax(value = "10.0", message = "Effect score cannot be greater than 10")
    private BigDecimal effectScore;

    @NotNull(message = "Ease score cannot be null")
    @DecimalMin(value = "0.0", message = "Ease score cannot be less than 0")
    @DecimalMax(value = "10.0", message = "Ease score cannot be greater than 10")
    private BigDecimal easeScore;

    @NotNull(message = "Stability score cannot be null")
    @DecimalMin(value = "0.0", message = "Stability score cannot be less than 0")
    @DecimalMax(value = "10.0", message = "Stability score cannot be greater than 10")
    private BigDecimal stabilityScore;

    @NotNull(message = "Recommend score cannot be null")
    @DecimalMin(value = "0.0", message = "Recommend score cannot be less than 0")
    @DecimalMax(value = "10.0", message = "Recommend score cannot be greater than 10")
    private BigDecimal recommendScore;

    private String comment;
}
