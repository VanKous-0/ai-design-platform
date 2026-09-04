package com.project.modules.prompt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prompt_revision")
public class PromptRevision {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long promptId;

    private Integer revisionNo;

    private String content;

    private String inputDesc;

    private String outputDesc;

    private String tips;

    private String exampleInput;

    private String exampleOutput;

    private String parameterSchemaJson;

    private Long createdBy;

    private String status;

    private LocalDateTime createTime;
}
