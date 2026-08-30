package com.project.modules.site.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AwardRecordVO {

    private Long id;

    private String title;

    private String awardLevel;

    private String issuer;

    private LocalDate awardDate;

    private String summary;

    private String imageUrl;

    private String linkUrl;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
