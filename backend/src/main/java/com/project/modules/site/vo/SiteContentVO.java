package com.project.modules.site.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SiteContentVO {

    private Long id;

    private String sectionKey;

    private String title;

    private String subtitle;

    private String content;

    private String imageUrl;

    private String linkUrl;

    private String extraJson;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
