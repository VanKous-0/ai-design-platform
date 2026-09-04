package com.project.modules.prompt.model;

public record PromptParameterSnapshot(
        String paramKey,
        String paramName,
        String paramType,
        Integer required,
        String defaultValue,
        String placeholder,
        Integer sortOrder
) {
}
