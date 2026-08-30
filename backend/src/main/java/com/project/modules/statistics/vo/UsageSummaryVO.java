package com.project.modules.statistics.vo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageSummaryVO {

    private Long totalEventCount;

    private Long loginUserEventCount;

    private Long anonymousEventCount;

    private Long uniqueUserCount;

    private Long uniqueAnonymousCount;

    private Long totalStayDuration;

    private BigDecimal averageStayDuration;
}
