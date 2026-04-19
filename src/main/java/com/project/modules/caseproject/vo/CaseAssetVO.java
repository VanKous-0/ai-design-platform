package com.project.modules.caseproject.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CaseAssetVO {

    private Long id;

    private Long caseId;

    private String assetType;

    private String assetUrl;

    private String title;

    private String description;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
