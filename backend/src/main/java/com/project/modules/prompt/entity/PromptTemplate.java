package com.project.modules.prompt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prompt_template")
public class PromptTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long stageId;

    private Long ownerUserId;

    private String ownershipType;

    private Long currentRevisionId;

    private String title;

    private String code;

    private String category;

    private String content;

    private String inputDesc;

    private String outputDesc;

    private String tips;

    private String exampleInput;

    private String exampleOutput;

    private String sourceDesc;

    private String sourceType;

    private String sourceFile;

    private String sourcePage;

    private Integer sortOrder;

    private Integer copyCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
