package com.project.modules.tool.service;

import com.project.modules.tool.dto.AiToolCreateRequest;
import com.project.modules.tool.dto.AiToolUpdateRequest;
import com.project.modules.tool.dto.ToolEvaluationSaveRequest;
import com.project.modules.tool.dto.ToolStageSetRequest;
import com.project.modules.tool.vo.AiToolEvaluationVO;
import com.project.modules.tool.vo.AiToolVO;

import java.util.List;

public interface AiToolService {

    List<AiToolVO> listTools(Long stageId, String keyword);

    AiToolVO getToolDetail(Long id);

    List<AiToolEvaluationVO> listEvaluations(Long toolId);

    List<AiToolVO> recommendTools(Long stageId);

    AiToolVO createTool(AiToolCreateRequest request);

    AiToolVO updateTool(Long id, AiToolUpdateRequest request);

    void deleteTool(Long id);

    List<Long> setToolStages(Long toolId, ToolStageSetRequest request);

    List<AiToolEvaluationVO> saveToolEvaluations(Long toolId, ToolEvaluationSaveRequest request);
}
