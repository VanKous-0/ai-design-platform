package com.project.modules.tool.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AiToolEvaluationVO {

    private Long dimensionId;

    private String dimensionName;

    private String dimensionCode;

    private String dimensionDescription;

    private BigDecimal weightPercent;

    private Integer sortOrder;

    private BigDecimal score;

    private String comment;

    private String dataStatus;

    private String sourceDesc;
}
