package com.project.modules.workflow.runtime.vo;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowNextStepVO {

    private Long nextNodeId;

    private String nextNodeName;

    private String nextTip;

    private Boolean whetherFinished;
}
