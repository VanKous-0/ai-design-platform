package com.project.modules.prompt.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PromptRenderRequest {

    private Long promptRevisionId;

    private Map<String, String> parameters;
}
