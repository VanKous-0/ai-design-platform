package com.project.modules.prompt.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromptToolVO {

    private Long id;

    private String name;

    private String code;

    private String officialUrl;

    private String logoUrl;
}
