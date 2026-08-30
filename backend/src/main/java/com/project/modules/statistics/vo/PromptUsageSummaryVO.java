package com.project.modules.statistics.vo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptUsageSummaryVO {

    private Long promptId;

    private String promptTitle;

    private Integer copyCount;

    private Long renderCount;

    private Long eventCount;
}
