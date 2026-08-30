package com.project.modules.tool.controller;

import com.project.common.result.Result;
import com.project.modules.tool.dto.AiToolCreateRequest;
import com.project.modules.tool.dto.AiToolUpdateRequest;
import com.project.modules.tool.dto.ToolEvaluationSaveRequest;
import com.project.modules.tool.dto.ToolStageSetRequest;
import com.project.modules.tool.service.AiToolService;
import com.project.modules.tool.vo.AiToolEvaluationVO;
import com.project.modules.tool.vo.AiToolVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tools")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAiToolController {

    private final AiToolService aiToolService;

    public AdminAiToolController(AiToolService aiToolService) {
        this.aiToolService = aiToolService;
    }

    @PostMapping
    public Result<AiToolVO> createTool(@Valid @RequestBody AiToolCreateRequest request) {
        return Result.success(aiToolService.createTool(request));
    }

    @PutMapping("/{id}")
    public Result<AiToolVO> updateTool(
            @PathVariable Long id,
            @Valid @RequestBody AiToolUpdateRequest request
    ) {
        return Result.success(aiToolService.updateTool(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTool(@PathVariable Long id) {
        aiToolService.deleteTool(id);
        return Result.success();
    }

    @PostMapping("/{id}/stages")
    public Result<List<Long>> setToolStages(
            @PathVariable Long id,
            @Valid @RequestBody ToolStageSetRequest request
    ) {
        return Result.success(aiToolService.setToolStages(id, request));
    }

    @PostMapping("/{id}/evaluations")
    public Result<List<AiToolEvaluationVO>> saveToolEvaluations(
            @PathVariable Long id,
            @Valid @RequestBody ToolEvaluationSaveRequest request
    ) {
        return Result.success(aiToolService.saveToolEvaluations(id, request));
    }
}
