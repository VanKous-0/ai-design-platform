package com.project.modules.caseproject.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaseToolUsageVO {

    private Long id;

    private Long toolId;

    private String toolName;

    private String toolCode;

    private String toolType;

    private String usageStage;

    private String usageDesc;

    private Integer sortOrder;
}
