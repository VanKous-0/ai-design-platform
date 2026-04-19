package com.project.modules.prompt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PromptToolSetRequest {

    @NotNull(message = "工具ID列表不能为空")
    private List<Long> toolIds;
}
