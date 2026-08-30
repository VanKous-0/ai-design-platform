package com.project.modules.workflow.runtime.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkflowTemplateNodeVO {

    private Long id;

    private Long templateId;

    private Long stageId;

    private Long stepId;

    private String nodeName;

    private String nodeCode;

    private String nodeType;

    private String inputDesc;

    private String outputDesc;

    private String nextTip;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
