package com.project.modules.workflow.runtime.service;

import com.project.modules.workflow.runtime.dto.WorkflowInstanceCreateRequest;
import com.project.modules.workflow.runtime.dto.WorkflowStepCompleteRequest;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceDetailVO;
import com.project.modules.workflow.runtime.vo.WorkflowInstanceListVO;
import com.project.modules.workflow.runtime.vo.WorkflowNextStepVO;
import com.project.modules.workflow.runtime.vo.WorkflowProgressVO;
import com.project.modules.workflow.runtime.vo.WorkflowStepCompleteVO;

import java.util.List;

public interface WorkflowInstanceService {

    WorkflowInstanceDetailVO createInstance(Long userId, WorkflowInstanceCreateRequest request);

    List<WorkflowInstanceListVO> listMyInstances(Long userId);

    WorkflowInstanceDetailVO getMyInstance(Long userId, Long instanceId);

    WorkflowProgressVO getProgress(Long userId, Long instanceId);

    WorkflowNextStepVO getNextStep(Long userId, Long instanceId);

    WorkflowStepCompleteVO completeStep(Long userId, Long instanceId, Long nodeId, WorkflowStepCompleteRequest request);
}
