package com.project.modules.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PromptPreferenceHintRequest {

    @NotBlank(message = "Preference key cannot be blank")
    @Size(max = 80, message = "Preference key cannot exceed 80 characters")
    @Pattern(
            regexp = "[a-z0-9][a-z0-9_.-]*",
            message = "Preference key must be a stable lowercase identifier"
    )
    private String preferenceKey;

    @NotBlank(message = "Preference value cannot be blank")
    @Size(max = 1000, message = "Preference value cannot exceed 1000 characters")
    private String preferenceValue;
}
