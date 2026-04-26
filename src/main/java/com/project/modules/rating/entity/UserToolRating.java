package com.project.modules.rating.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_tool_rating")
public class UserToolRating {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long toolId;

    private BigDecimal effectScore;

    private BigDecimal easeScore;

    private BigDecimal stabilityScore;

    private BigDecimal recommendScore;

    private String comment;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
