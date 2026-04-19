package com.project.modules.tool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_tool_stage_rel")
public class AiToolStageRel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long toolId;

    private Long stageId;

    private LocalDateTime createTime;
}
