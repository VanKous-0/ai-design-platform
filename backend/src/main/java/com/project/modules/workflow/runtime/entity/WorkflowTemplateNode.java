package com.project.modules.workflow.runtime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workflow_template_node")
public class WorkflowTemplateNode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    private Long stageId;

    private Long stepId;

    private String nodeName;

    private String nodeCode;

    private String nodeType;

    private String inputDesc;

    private String outputDesc;

    private String nextTip;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
