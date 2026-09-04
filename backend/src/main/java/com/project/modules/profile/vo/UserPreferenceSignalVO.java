package com.project.modules.profile.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserPreferenceSignalVO {

    private Long id;

    private Long userId;

    private String preferenceKey;

    private String preferenceValue;

    private String sentiment;

    private String scope;

    private String source;

    private BigDecimal confidence;

    private Integer evidenceCount;

    private String evidenceSummary;

    private LocalDateTime lastObservedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
