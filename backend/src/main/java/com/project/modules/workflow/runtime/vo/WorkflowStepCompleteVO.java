package com.project.modules.workflow.runtime.vo;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStepCompleteVO {

    private Long instanceId;

    private Long completedNodeId;

    private BigDecimal progress;

    private WorkflowNextStepVO nextStep;
}
