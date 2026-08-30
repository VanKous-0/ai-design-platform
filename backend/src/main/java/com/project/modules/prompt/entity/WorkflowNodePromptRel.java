package com.project.modules.prompt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workflow_node_prompt_rel")
public class WorkflowNodePromptRel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long nodeId;

    private Long promptId;

    private Integer sortOrder;

    private LocalDateTime createTime;
}
