package com.project.modules.workflow.runtime.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WorkflowProgressVO {

    private Long instanceId;

    private Integer totalNodeCount;

    private Integer completedNodeCount;

    private BigDecimal progress;

    private String status;

    private Long currentNodeId;

    private String currentNodeName;
}
