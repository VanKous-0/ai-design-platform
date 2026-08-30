package com.project.modules.workflow.runtime.service;

import com.project.common.result.PageResult;
import com.project.modules.workflow.runtime.dto.WorkflowInstanceCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepCompleteRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepIterationCreateRequest;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceListVO;
import com.project.modules.workflow.runtime.vo.WorkflowNextStepVO;
import com.project.modules.workflow.runtime.vo.WorkflowProgressVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepCompleteVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepIterationVO;

import java.util.List;

public interface WorkflowInstanceService {

    WorkflowInstanceDetailVO createInstance(Long userId, WorkflowInstanceCreateRequest request);

    List<WorkflowInstanceListVO> listMyInstances(Long userId);

    PageResult<WorkflowInstanceListVO> pageMyInstances(Long userId, Long pageNum, Long pageSize);

    WorkflowInstanceDetailVO getMyInstance(Long userId, Long instanceId);

    WorkflowProgressVO getProgress(Long userId, Long instanceId);

    WorkflowNextStepVO getNextStep(Long userId, Long instanceId);

    WorkflowStepCompleteVO completeStep(Long userId, Long instanceId, Long nodeId, WorkflowStepCompleteRequest request);

    WorkflowStepIterationVO createStepIteration(
            Long userId,
            Long instanceId,
            Long nodeId,
            WorkflowStepIterationCreateRequest request
    );

    List<WorkflowStepIterationVO> listStepIterations(Long userId, Long instanceId, Long nodeId);

    WorkflowStepIterationVO selectStepIteration(Long userId, Long instanceId, Long nodeId, Long iterationId);
}
