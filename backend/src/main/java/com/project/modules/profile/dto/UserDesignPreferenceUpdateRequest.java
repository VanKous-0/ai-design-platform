package com.project.modules.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDesignPreferenceUpdateRequest {

    @Size(max = 100, message = "项目类型不能超过100个字符")
    private String preferredProjectType;

    @Size(max = 100, message = "设计风格不能超过100个字符")
    private String preferredStyle;

    @Size(max = 100, message = "场地尺度不能超过100个字符")
    private String preferredSiteScale;

    @Size(max = 100, message = "目标用户不能超过100个字符")
    private String preferredTargetUser;

    private Long defaultToolId;

    private String extraJson;
}
