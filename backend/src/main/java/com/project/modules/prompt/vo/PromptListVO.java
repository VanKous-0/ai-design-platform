package com.project.modules.prompt.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PromptListVO {

    private Long id;

    private Long stageId;

    private Long ownerUserId;

    private String ownershipType;

    private Long currentRevisionId;

    private String title;

    private String code;

    private String category;

    private String content;

    private String sourceType;

    private String sourceFile;

    private String sourcePage;

    private Integer sortOrder;

    private Integer copyCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
