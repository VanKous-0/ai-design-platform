package com.project.modules.tool.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ToolStageSetRequest {

    @NotNull(message = "阶段ID列表不能为空")
    private List<Long> stageIds;
}
