package com.project.modules.caseproject.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCaseUpdateRequest {

    @NotBlank(message = "案例标题不能为空")
    @Size(max = 150, message = "案例标题不能超过150个字符")
    private String title;

    @NotBlank(message = "案例编码不能为空")
    @Size(max = 80, message = "案例编码不能超过80个字符")
    private String code;

    @NotNull(message = "阶段ID不能为空")
    private Long stageId;

    private Long toolId;

    @Size(max = 500, message = "封面地址不能超过500个字符")
    private String coverUrl;

    @Size(max = 500, message = "案例摘要不能超过500个字符")
    private String summary;

    private String content;

    @Size(max = 255, message = "来源说明不能超过255个字符")
    private String sourceDesc;

    @Size(max = 100, message = "作者名称不能超过100个字符")
    private String authorName;

    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;
}
