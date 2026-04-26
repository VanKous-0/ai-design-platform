package com.project.modules.prompt.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PromptParameterVO {

    private Long id;

    private Long promptId;

    private String paramKey;

    private String paramName;

    private String paramType;

    private Boolean required;

    private String defaultValue;

    private String placeholder;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
