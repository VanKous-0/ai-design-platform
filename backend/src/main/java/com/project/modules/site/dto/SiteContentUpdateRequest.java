package com.project.modules.site.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SiteContentUpdateRequest {

    @NotBlank(message = "sectionKey must not be blank")
    private String sectionKey;

    @NotBlank(message = "title must not be blank")
    private String title;

    private String subtitle;

    private String content;

    private String imageUrl;

    private String linkUrl;

    private String extraJson;

    @Min(value = 0, message = "sortOrder must be greater than or equal to 0")
    private Integer sortOrder;

    private Integer status;
}
