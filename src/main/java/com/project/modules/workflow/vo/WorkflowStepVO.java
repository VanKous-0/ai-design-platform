package com.project.modules.workflow.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkflowStepVO {

    private Long id;

    private Long stageId;

    private String title;

    private String content;

    private String inputDesc;

    private String outputDesc;

    private String tips;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
