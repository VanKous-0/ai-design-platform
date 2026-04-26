package com.project.modules.statistics.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UsageEventVO {

    private Long id;

    private Long userId;

    private String anonymousId;

    private String eventType;

    private String targetType;

    private Long targetId;

    private String pageUrl;

    private Integer stayDuration;

    private String inputSummary;

    private String extraJson;

    private LocalDateTime createTime;
}
