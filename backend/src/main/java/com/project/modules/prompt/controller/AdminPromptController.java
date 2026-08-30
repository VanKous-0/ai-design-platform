package com.project.modules.prompt.controller;

import com.project.common.result.Result;
import com.project.modules.prompt.dto.PromptCreateRequest;
import com.project.modules.prompt.dto.PromptParameterCreateRequest;
import com.project.modules.prompt.dto.PromptToolSetRequest;
import com.project.modules.prompt.dto.PromptUpdateRequest;
import com.project.modules.prompt.service.PromptService;
import com.project.modules.prompt.vo.PromptDetailVO;
import com.project.modules.prompt.vo.PromptParameterVO;
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
@RequestMapping("/api/admin/prompts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromptController {

    private final PromptService promptService;

    public AdminPromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @PostMapping
    public Result<PromptDetailVO> createPrompt(@Valid @RequestBody PromptCreateRequest request) {
        return Result.success(promptService.createPrompt(request));
    }

    @PutMapping("/{id}")
    public Result<PromptDetailVO> updatePrompt(
            @PathVariable Long id,
            @Valid @RequestBody PromptUpdateRequest request
    ) {
        return Result.success(promptService.updatePrompt(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePrompt(@PathVariable Long id) {
        promptService.deletePrompt(id);
        return Result.success();
    }

    @PostMapping("/{id}/tools")
    public Result<List<Long>> setPromptTools(
            @PathVariable Long id,
            @Valid @RequestBody PromptToolSetRequest request
    ) {
        return Result.success(promptService.setPromptTools(id, request));
    }

    @PostMapping("/{id}/parameters")
    public Result<PromptParameterVO> createParameter(
            @PathVariable Long id,
            @Valid @RequestBody PromptParameterCreateRequest request
    ) {
        return Result.success(promptService.createParameter(id, request));
    }

}
