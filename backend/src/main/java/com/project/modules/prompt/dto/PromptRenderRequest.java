package com.project.modules.prompt.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PromptRenderRequest {

    private Map<String, String> parameters;
}
