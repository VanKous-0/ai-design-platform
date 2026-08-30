package com.project.modules.statistics.vo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetTypeSummaryVO {

    private String targetType;

    private Long count;
}
