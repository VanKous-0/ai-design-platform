package com.project.modules.workflow.runtime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workflow_step_iteration")
public class WorkflowStepIteration {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long instanceId;

    private Long nodeId;

    private Long userId;

    private Integer iterationNo;

    private Long toolId;

    private String promptContent;

    private String outputContent;

    private String resultUrl;

    private Integer effectScore;

    private Integer accuracyScore;

    private Integer controllabilityScore;

    private Integer usabilityScore;

    private String improvementNote;

    private Integer selected;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
