package com.project.modules.prompt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prompt_tool_rel")
public class PromptToolRel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long promptId;

    private Long toolId;

    private LocalDateTime createTime;
}
