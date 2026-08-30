package com.project.modules.statistics.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SurveyFeedbackVO {

    private Long id;

    private Long userId;

    private String anonymousId;

    private String scene;

    private BigDecimal score;

    private String content;

    private String contact;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
