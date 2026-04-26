package com.project.modules.workflow.runtime.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkflowInstanceDetailVO {

    private Long id;

    private Long templateId;

    private String templateName;

    private Long userId;

    private String title;

    private Long currentNodeId;

    private String currentNodeName;

    private String status;

    private BigDecimal progress;

    private LocalDateTime startTime;

    private LocalDateTime finishTime;

    private List<WorkflowTemplateNodeVO> nodes;

    private List<WorkflowStepRecordVO> stepRecords;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
