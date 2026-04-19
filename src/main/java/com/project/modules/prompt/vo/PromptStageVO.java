package com.project.modules.prompt.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromptStageVO {

    private Long id;

    private String name;

    private String code;
}
