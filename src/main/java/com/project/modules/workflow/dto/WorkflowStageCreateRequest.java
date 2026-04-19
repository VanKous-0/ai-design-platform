package com.project.modules.workflow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowStageCreateRequest {

    @NotBlank(message = "阶段名称不能为空")
    private String name;

    @NotBlank(message = "阶段编码不能为空")
    private String code;

    private String description;

    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;

    private Integer status;
}
