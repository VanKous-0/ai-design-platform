package com.project.modules.caseproject.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CaseAssetUpdateRequest {

    @NotBlank(message = "资源类型不能为空")
    private String assetType;

    @NotBlank(message = "资源地址不能为空")
    private String assetUrl;

    private String title;

    private String description;

    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;
}
