package com.project.modules.prompt.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PromptDetailVO {

    private Long id;

    private Long stageId;

    private Long ownerUserId;

    private String ownershipType;

    private Long currentRevisionId;

    private String title;

    private String code;

    private String category;

    private String content;

    private String inputDesc;

    private String outputDesc;

    private String tips;

    private String exampleInput;

    private String exampleOutput;

    private String sourceDesc;

    private String sourceType;

    private String sourceFile;

    private String sourcePage;

    private Integer sortOrder;

    private Integer copyCount;

    private Integer status;

    private PromptStageVO stage;

    private List<PromptToolVO> tools;

    private List<PromptPreferenceHintVO> preferenceHints;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
