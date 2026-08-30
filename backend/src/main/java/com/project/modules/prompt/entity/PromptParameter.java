package com.project.modules.prompt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prompt_parameter")
public class PromptParameter {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long promptId;

    private String paramKey;

    private String paramName;

    private String paramType;

    private Integer required;

    private String defaultValue;

    private String placeholder;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
