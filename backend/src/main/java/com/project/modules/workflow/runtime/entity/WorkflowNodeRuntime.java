package com.project.modules.workflow.runtime.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workflow_node_runtime")
public class WorkflowNodeRuntime {

    private Long instanceId;
    private Long nodeId;
    private Integer nextIterationNo;
    private Long selectedIterationId;
    private Long lockVersion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
