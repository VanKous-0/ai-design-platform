package com.project.modules.workflow.runtime.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkflowTemplateDetailVO {

    private Long id;

    private String name;

    private String code;

    private String description;

    private String sceneType;

    private String coverUrl;

    private Integer sortOrder;

    private Integer status;

    private List<WorkflowTemplateNodeVO> nodes;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
