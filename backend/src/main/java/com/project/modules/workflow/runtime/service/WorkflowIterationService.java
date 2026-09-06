package com.project.modules.workflow.runtime.service;

import com.project.modules.workflow.runtime.dto.WorkflowStepIterationCreateRequest;
import com.project.modules.workflow.runtime.vo.WorkflowStepIterationVO;

import java.util.List;

public interface WorkflowIterationService {

    WorkflowStepIterationVO create(
            Long userId,
            Long instanceId,
            Long nodeId,
            WorkflowStepIterationCreateRequest request,
            String idempotencyKey
    );

    List<WorkflowStepIterationVO> list(Long userId, Long instanceId, Long nodeId);

    WorkflowStepIterationVO select(Long userId, Long instanceId, Long nodeId, Long iterationId);
}
