package com.project.modules.prompt.controller;

import com.project.common.result.Result;
import com.project.modules.prompt.dto.PromptRenderRequest;
import com.project.modules.prompt.service.PromptService;
import com.project.modules.prompt.vo.PromptDetailVO;
import com.project.modules.prompt.vo.PromptListVO;
import com.project.modules.prompt.vo.PromptParameterVO;
import com.project.modules.prompt.vo.PromptRenderVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prompts")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @GetMapping
    public Result<?> listPrompts(
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize
    ) {
        if (pageNum != null || pageSize != null) {
            return Result.success(promptService.pagePrompts(
                    stageId,
                    category,
                    keyword,
                    sourceType,
                    pageNum,
                    pageSize
            ));
        }
        return Result.success(promptService.listPrompts(stageId, category, keyword, sourceType));
    }

    @GetMapping("/by-node")
    public Result<List<PromptListVO>> listPromptsByNode(@RequestParam Long nodeId) {
        return Result.success(promptService.listPromptsByNode(nodeId));
    }

    @GetMapping("/search")
    public Result<List<PromptListVO>> searchPrompts(@RequestParam String keyword) {
        return Result.success(promptService.searchPrompts(keyword));
    }

    @GetMapping("/recommend")
    public Result<List<PromptListVO>> recommendPrompts(
            @RequestParam Long stageId,
            @RequestParam(required = false) Long toolId
    ) {
        return Result.success(promptService.recommendPrompts(stageId, toolId));
    }

    @GetMapping("/{id}")
    public Result<PromptDetailVO> getPrompt(@PathVariable Long id) {
        return Result.success(promptService.getPromptDetail(id));
    }

    @GetMapping("/{id}/parameters")
    public Result<List<PromptParameterVO>> listParameters(@PathVariable Long id) {
        return Result.success(promptService.listParameters(id));
    }

    @PostMapping("/{id}/render")
    public Result<PromptRenderVO> renderPrompt(
            @PathVariable Long id,
            @Valid @RequestBody PromptRenderRequest request
    ) {
        return Result.success(promptService.renderPrompt(id, request));
    }

    @PostMapping("/{id}/copy")
    public Result<Void> copyPrompt(@PathVariable Long id) {
        promptService.copyPrompt(id);
        return Result.success();
    }
}
