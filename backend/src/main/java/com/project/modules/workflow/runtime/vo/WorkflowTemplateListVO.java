package com.project.modules.workflow.runtime.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkflowTemplateListVO {

    private Long id;

    private String name;

    private String code;

    private String description;

    private String sceneType;

    private String coverUrl;

    private Integer nodeCount;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
