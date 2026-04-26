package com.project.modules.statistics.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WorkflowRatingStatisticsVO {

    private Long templateId;

    private String templateName;

    private Long ratingCount;

    private BigDecimal averageEffectScore;

    private BigDecimal averageEaseScore;

    private BigDecimal averageStabilityScore;

    private BigDecimal averageRecommendScore;

    private BigDecimal averageTotalScore;
}
