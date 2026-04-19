package com.project.modules.caseproject.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CaseProjectUpdateRequest {

    @NotBlank(message = "案例标题不能为空")
    private String title;

    @NotBlank(message = "案例编码不能为空")
    private String code;

    @NotNull(message = "阶段ID不能为空")
    private Long stageId;

    private Long toolId;

    private String coverUrl;

    private String summary;

    private String content;

    private String sourceDesc;

    private String authorName;

    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;

    private Integer status;
}
