package com.project.modules.profile.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserPreferenceSignalUpsertRequest {

    @NotBlank(message = "Preference key cannot be blank")
    @Size(max = 80, message = "Preference key cannot exceed 80 characters")
    private String preferenceKey;

    @NotBlank(message = "Preference value cannot be blank")
    @Size(max = 1000, message = "Preference value cannot exceed 1000 characters")
    private String preferenceValue;

    @Size(max = 20, message = "Sentiment cannot exceed 20 characters")
    private String sentiment;

    @NotBlank(message = "Preference scope cannot be blank")
    @Size(max = 20, message = "Preference scope cannot exceed 20 characters")
    private String scope;

    @NotBlank(message = "Preference source cannot be blank")
    @Size(max = 30, message = "Preference source cannot exceed 30 characters")
    private String source;

    @DecimalMin(value = "0.0", message = "Confidence cannot be below 0")
    @DecimalMax(value = "1.0", message = "Confidence cannot exceed 1")
    private BigDecimal confidence;

    @Size(max = 1000, message = "Evidence summary cannot exceed 1000 characters")
    private String evidenceSummary;

    @PastOrPresent(message = "Observed time cannot be in the future")
    private LocalDateTime observedAt;
}
