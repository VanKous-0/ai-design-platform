package com.project.modules.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReviewRecordCreateRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "复盘标题不能为空")
    private String title;

    @NotBlank(message = "复盘编码不能为空")
    private String code;

    @NotNull(message = "阶段ID不能为空")
    private Long stageId;

    private Long toolId;

    private String projectName;

    private String summary;

    private String problemDesc;

    private String solutionDesc;

    private String reflection;

    @DecimalMin(value = "0.0", message = "评分不能小于0")
    @DecimalMax(value = "10.0", message = "评分不能大于10")
    private BigDecimal score;

    private LocalDate reviewDate;

    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;

    private Integer status;
}
