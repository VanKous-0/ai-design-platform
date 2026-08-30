package com.project.modules.tool.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiToolCreateRequest {

    @NotBlank(message = "工具名称不能为空")
    private String name;

    @NotBlank(message = "工具编码不能为空")
    private String code;

    private String officialUrl;

    private String logoUrl;

    private String description;

    private String priceDesc;

    private String versionDesc;

    private String dataStatus;

    private String sourceDesc;

    private Integer status;
}
