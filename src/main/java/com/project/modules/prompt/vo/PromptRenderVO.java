package com.project.modules.prompt.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PromptRenderVO {

    private Long promptId;

    private String title;

    private String renderedContent;

    private List<String> missingRequiredParams;
}
