package com.project.modules.statistics.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromptUsageSummaryVO {

    private Long promptId;

    private String promptTitle;

    private Integer copyCount;

    private Long renderCount;

    private Long eventCount;
}
