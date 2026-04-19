package com.project.modules.prompt.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PromptCreateRequest {

    @NotNull(message = "阶段ID不能为空")
    private Long stageId;

    @NotBlank(message = "提示词标题不能为空")
    private String title;

    @NotBlank(message = "提示词编码不能为空")
    private String code;

    @NotBlank(message = "提示词分类不能为空")
    private String category;

    @NotBlank(message = "提示词正文不能为空")
    private String content;

    private String inputDesc;

    private String outputDesc;

    private String tips;

    private String exampleInput;

    private String exampleOutput;

    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;

    private Integer status;
}
