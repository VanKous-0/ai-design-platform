package com.project.modules.statistics.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TargetTypeSummaryVO {

    private String targetType;

    private Long count;
}
