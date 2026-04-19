package com.project.modules.caseproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("case_project")
public class CaseProject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String code;

    private Long stageId;

    private Long toolId;

    private String coverUrl;

    private String summary;

    private String content;

    private String sourceDesc;

    private String authorName;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
