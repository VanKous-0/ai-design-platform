package com.project.modules.tool.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AiToolVO {

    private Long id;

    private String name;

    private String code;

    private String officialUrl;

    private String logoUrl;

    private String description;

    private String priceDesc;

    private String versionDesc;

    private String dataStatus;

    private String sourceDesc;

    private Integer status;

    private List<ToolStageVO> stages;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
