package com.project.modules.workflow.runtime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("workflow_instance")
public class WorkflowInstance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    private Long userId;

    private String title;

    private Long currentNodeId;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime finishTime;

    private BigDecimal progress;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
