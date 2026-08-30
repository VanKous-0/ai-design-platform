package com.project.modules.workflow.runtime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workflow_step_record")
public class WorkflowStepRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long instanceId;

    private Long nodeId;

    private Long userId;

    private String inputContent;

    private String outputContent;

    private String status;

    private Integer durationSeconds;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String nextSuggestion;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
