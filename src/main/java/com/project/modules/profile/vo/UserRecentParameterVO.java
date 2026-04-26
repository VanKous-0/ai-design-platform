package com.project.modules.profile.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserRecentParameterVO {

    private Long id;

    private Long userId;

    private String parameterType;

    private String parameterKey;

    private String parameterValue;

    private String source;

    private Integer useCount;

    private LocalDateTime lastUsedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
