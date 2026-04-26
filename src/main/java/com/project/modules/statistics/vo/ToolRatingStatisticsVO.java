package com.project.modules.statistics.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ToolRatingStatisticsVO {

    private Long toolId;

    private String toolName;

    private Long ratingCount;

    private BigDecimal averageEffectScore;

    private BigDecimal averageEaseScore;

    private BigDecimal averageStabilityScore;

    private BigDecimal averageRecommendScore;

    private BigDecimal averageTotalScore;
}
