package com.project.modules.caseproject.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CaseAssetUpdateRequest {

    @NotBlank(message = "资源类型不能为空")
    @Size(max = 20, message = "资源类型不能超过20个字符")
    private String assetType;

    @NotBlank(message = "资源地址不能为空")
    @Size(max = 500, message = "资源地址不能超过500个字符")
    private String assetUrl;

    @Size(max = 150, message = "资源标题不能超过150个字符")
    private String title;

    @Size(max = 500, message = "资源说明不能超过500个字符")
    private String description;

    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;
}
