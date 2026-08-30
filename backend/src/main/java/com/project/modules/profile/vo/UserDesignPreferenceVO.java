package com.project.modules.profile.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDesignPreferenceVO {

    private Long id;

    private Long userId;

    private String preferredProjectType;

    private String preferredStyle;

    private String preferredSiteScale;

    private String preferredTargetUser;

    private Long defaultToolId;

    private String extraJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
