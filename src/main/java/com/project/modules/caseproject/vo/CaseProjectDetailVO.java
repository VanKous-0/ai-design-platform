package com.project.modules.caseproject.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CaseProjectDetailVO {

    private Long id;

    private String title;

    private String code;

    private Long stageId;

    private Long toolId;

    private String coverUrl;

    private String summary;

    private String content;

    private String sourceDesc;

    private String authorName;

    private Integer sortOrder;

    private Integer status;

    private CaseStageVO stage;

    private CaseToolVO tool;

    private List<CaseAssetVO> assets;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
