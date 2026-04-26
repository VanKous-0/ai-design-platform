package com.project.modules.statistics.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WorkflowStatisticsVO {

    private Long templateId;

    private String templateName;

    private Long instanceCount;

    private Long runningCount;

    private Long finishedCount;

    private BigDecimal averageProgress;

    private Long completeStepCount;
}
