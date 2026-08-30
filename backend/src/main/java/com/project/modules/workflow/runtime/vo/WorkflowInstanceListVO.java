package com.project.modules.workflow.runtime.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkflowInstanceListVO {

    private Long id;

    private Long templateId;

    private String templateName;

    private String title;

    private Long currentNodeId;

    private String currentNodeName;

    private String status;

    private BigDecimal progress;

    private LocalDateTime startTime;

    private LocalDateTime finishTime;

    private LocalDateTime createTime;
}
