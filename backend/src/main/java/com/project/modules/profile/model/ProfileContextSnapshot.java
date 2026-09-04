package com.project.modules.profile.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"schemaVersion", "preferences"})
public record ProfileContextSnapshot(
        int schemaVersion,
        List<ProfileContextPreference> preferences
) {
}
