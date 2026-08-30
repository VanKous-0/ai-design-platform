package com.project.modules.rating.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RatingSummaryVO {

    private Long targetId;

    private String targetType;

    private Long ratingCount;

    private BigDecimal averageEffectScore;

    private BigDecimal averageEaseScore;

    private BigDecimal averageStabilityScore;

    private BigDecimal averageRecommendScore;

    private BigDecimal averageTotalScore;
}
