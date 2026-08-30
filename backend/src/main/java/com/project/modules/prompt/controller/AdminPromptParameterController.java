package com.project.modules.prompt.controller;

import com.project.common.result.Result;
import com.project.modules.prompt.dto.PromptParameterUpdateRequest;
import com.project.modules.prompt.service.PromptService;
import com.project.modules.prompt.vo.PromptParameterVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/prompt-parameters")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromptParameterController {

    private final PromptService promptService;

    public AdminPromptParameterController(PromptService promptService) {
        this.promptService = promptService;
    }

    @PutMapping("/{id}")
    public Result<PromptParameterVO> updateParameter(
            @PathVariable Long id,
            @Valid @RequestBody PromptParameterUpdateRequest request
    ) {
        return Result.success(promptService.updateParameter(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteParameter(@PathVariable Long id) {
        promptService.deleteParameter(id);
        return Result.success();
    }
}
