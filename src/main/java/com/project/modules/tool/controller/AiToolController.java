package com.project.modules.tool.controller;

import com.project.common.result.Result;
import com.project.modules.tool.service.AiToolService;
import com.project.modules.tool.vo.AiToolEvaluationVO;
import com.project.modules.tool.vo.AiToolVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
public class AiToolController {

    private final AiToolService aiToolService;

    public AiToolController(AiToolService aiToolService) {
        this.aiToolService = aiToolService;
    }

    @GetMapping
    public Result<List<AiToolVO>> listTools(
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(aiToolService.listTools(stageId, keyword));
    }

    @GetMapping("/{id}")
    public Result<AiToolVO> getTool(@PathVariable Long id) {
        return Result.success(aiToolService.getToolDetail(id));
    }

    @GetMapping("/{id}/evaluations")
    public Result<List<AiToolEvaluationVO>> listEvaluations(@PathVariable Long id) {
        return Result.success(aiToolService.listEvaluations(id));
    }

    @GetMapping("/recommend")
    public Result<List<AiToolVO>> recommendTools(@RequestParam Long stageId) {
        return Result.success(aiToolService.recommendTools(stageId));
    }
}
