package com.project.modules.tool.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ToolEvaluationSaveRequest {

    @Valid
    @NotEmpty(message = "评分列表不能为空")
    private List<EvaluationItem> evaluations;

    @Data
    public static class EvaluationItem {

        @NotNull(message = "维度ID不能为空")
        private Long dimensionId;

        @NotNull(message = "评分不能为空")
        @DecimalMin(value = "0.0", message = "评分不能小于0")
        @DecimalMax(value = "10.0", message = "评分不能大于10")
        private BigDecimal score;

        private String comment;
    }
}
