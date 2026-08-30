package com.project.modules.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExperimentUserStatusUpdateRequest {

    @NotNull(message = "账号状态不能为空")
    @Min(value = 0, message = "账号状态只能为0或1")
    @Max(value = 1, message = "账号状态只能为0或1")
    private Integer status;
}
