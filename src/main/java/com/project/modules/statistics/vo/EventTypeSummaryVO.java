package com.project.modules.statistics.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventTypeSummaryVO {

    private String eventType;

    private Long count;
}
