package com.project.modules.prompt.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 500, message = "来源说明不能超过500个字符")
    private String sourceDesc;

    @Size(max = 30, message = "来源类型不能超过30个字符")
    private String sourceType;

    @Size(max = 255, message = "来源文件不能超过255个字符")
    private String sourceFile;

    @Size(max = 50, message = "来源页码不能超过50个字符")
    private String sourcePage;

    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;

    private Integer status;
}
