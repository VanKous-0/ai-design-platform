package com.project.modules.profile.dto;

import lombok.Data;

@Data
public class UserDesignPreferenceUpdateRequest {

    private String preferredProjectType;

    private String preferredStyle;

    private String preferredSiteScale;

    private String preferredTargetUser;

    private Long defaultToolId;

    private String extraJson;
}
