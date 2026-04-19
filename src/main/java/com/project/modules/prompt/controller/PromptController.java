package com.project.modules.prompt.controller;

import com.project.common.result.Result;
import com.project.modules.prompt.service.PromptService;
import com.project.modules.prompt.vo.PromptDetailVO;
import com.project.modules.prompt.vo.PromptListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public Result<List<PromptListVO>> listPrompts(
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(promptService.listPrompts(stageId, category, keyword));
    }

    @GetMapping("/{id}")
    public Result<PromptDetailVO> getPrompt(@PathVariable Long id) {
        return Result.success(promptService.getPromptDetail(id));
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

    @PostMapping("/{id}/copy")
    public Result<Void> copyPrompt(@PathVariable Long id) {
        promptService.copyPrompt(id);
        return Result.success();
    }
}
