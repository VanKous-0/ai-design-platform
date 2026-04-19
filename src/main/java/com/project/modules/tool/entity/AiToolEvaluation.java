package com.project.modules.tool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_tool_evaluation")
public class AiToolEvaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long toolId;

    private Long dimensionId;

    private BigDecimal score;

    private String comment;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
