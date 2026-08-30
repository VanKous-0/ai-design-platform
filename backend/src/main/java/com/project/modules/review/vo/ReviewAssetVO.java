package com.project.modules.review.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewAssetVO {

    private Long id;

    private Long reviewId;

    private String assetType;

    private String assetUrl;

    private String title;

    private String description;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
