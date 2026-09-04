package com.project.modules.profile.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"mappingId", "promptId", "preferenceKey", "preferenceValue"})
public record PreferenceEvidence(
        Long mappingId,
        Long promptId,
        String preferenceKey,
        String preferenceValue
) {
}
