package com.project.modules.caseproject.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CaseProjectListVO {

    private Long id;

    private String title;

    private String code;

    private Long stageId;

    private Long toolId;

    private String coverUrl;

    private String summary;

    private String sourceDesc;

    private String authorName;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
