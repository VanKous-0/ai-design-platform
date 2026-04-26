package com.project.modules.rating.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserWorkflowRatingVO {

    private Long id;

    private Long userId;

    private Long templateId;

    private Long instanceId;

    private BigDecimal effectScore;

    private BigDecimal easeScore;

    private BigDecimal stabilityScore;

    private BigDecimal recommendScore;

    private BigDecimal totalScore;

    private String comment;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
