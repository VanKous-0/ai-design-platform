package com.project.modules.workflow.service;

import com.project.modules.workflow.dto.WorkflowStageCreateRequest;
import com.project.modules.workflow.dto.WorkflowStageUpdateRequest;
import com.project.modules.workflow.dto.WorkflowStepCreateRequest;
import com.project.modules.workflow.dto.WorkflowStepUpdateRequest;
import com.project.modules.workflow.vo.WorkflowStageVO;
import com.project.modules.workflow.vo.WorkflowStepVO;

import java.util.List;

public interface WorkflowService {

    List<WorkflowStageVO> listEnabledStages();

    WorkflowStageVO getEnabledStage(Long id);

    List<WorkflowStepVO> listEnabledStepsByStageId(Long stageId);

    WorkflowStageVO createStage(WorkflowStageCreateRequest request);

    WorkflowStageVO updateStage(Long id, WorkflowStageUpdateRequest request);

    void deleteStage(Long id);

    WorkflowStepVO createStep(WorkflowStepCreateRequest request);

    WorkflowStepVO updateStep(Long id, WorkflowStepUpdateRequest request);

    void deleteStep(Long id);
}
