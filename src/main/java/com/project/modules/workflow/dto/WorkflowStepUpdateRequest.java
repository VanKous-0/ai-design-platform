package com.project.modules.workflow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkflowStepUpdateRequest {

    @NotNull(message = "阶段ID不能为空")
    private Long stageId;

    @NotBlank(message = "步骤标题不能为空")
    private String title;

    @NotBlank(message = "步骤内容不能为空")
    private String content;

    private String inputDesc;

    private String outputDesc;

    private String tips;

    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;

    private Integer status;
}
