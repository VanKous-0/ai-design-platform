package com.project.modules.workflow.runtime.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowNextStepVO {

    private Long nextNodeId;

    private String nextNodeName;

    private String nextTip;

    private Boolean whetherFinished;
}
