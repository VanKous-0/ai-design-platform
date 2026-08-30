package com.project.modules.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExperimentUserBatchCreateRequest {

    @NotBlank(message = "实验批次不能为空")
    @Size(max = 50, message = "实验批次不能超过50个字符")
    private String experimentBatch;

    @NotBlank(message = "实验分组不能为空")
    @Size(max = 50, message = "实验分组不能超过50个字符")
    private String experimentGroup;

    @NotBlank(message = "用户名前缀不能为空")
    @Size(max = 30, message = "用户名前缀不能超过30个字符")
    private String usernamePrefix;

    @Size(max = 30, message = "实验编号前缀不能超过30个字符")
    private String experimentCodePrefix;

    @Min(value = 1, message = "起始序号不能小于1")
    private Integer startNumber = 1;

    @Min(value = 1, message = "创建数量不能小于1")
    @Max(value = 100, message = "单次最多创建100个账号")
    @NotNull(message = "创建数量不能为空")
    private Integer count;

    @NotBlank(message = "初始密码不能为空")
    @Size(min = 8, max = 72, message = "初始密码长度必须在8到72个字符之间")
    private String initialPassword;
}
