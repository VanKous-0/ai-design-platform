package com.project.modules.workflow.runtime.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkflowStepIterationVO {

    private Long id;

    private Long instanceId;

    private Long nodeId;

    private Integer iterationNo;

    private Long toolId;

    private String toolName;

    private String promptContent;

    private String outputContent;

    private String resultUrl;

    private Integer effectScore;

    private Integer accuracyScore;

    private Integer controllabilityScore;

    private Integer usabilityScore;

    private BigDecimal averageScore;

    private String improvementNote;

    private Boolean selected;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
