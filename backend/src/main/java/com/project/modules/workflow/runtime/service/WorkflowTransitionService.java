package com.project.modules.workflow.runtime.service;

import com.project.modules.workflow.runtime.dto.WorkflowStepCompleteRequest;
import com.project.modules.workflow.runtime.vo.WorkflowStepCompleteVO;

public interface WorkflowTransitionService {

    WorkflowStepCompleteVO completeStep(
            Long userId,
            Long instanceId,
            Long nodeId,
            WorkflowStepCompleteRequest request,
            String idempotencyKey
    );
}
