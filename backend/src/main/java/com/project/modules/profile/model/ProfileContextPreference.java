package com.project.modules.profile.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonPropertyOrder({
        "signalId", "preferenceKey", "preferenceValue", "sentiment", "scope",
        "source", "confidence", "lastObservedAt"
})
public record ProfileContextPreference(
        Long signalId,
        String preferenceKey,
        String preferenceValue,
        String sentiment,
        String scope,
        String source,
        BigDecimal confidence,
        LocalDateTime lastObservedAt
) {
}
