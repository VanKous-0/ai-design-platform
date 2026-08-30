package com.project.modules.caseproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("case_tool_usage")
public class CaseToolUsage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long caseId;

    private Long toolId;

    private String toolName;

    private String toolCode;

    private String toolType;

    private String usageStage;

    private String usageDesc;

    private Integer sortOrder;

    private LocalDateTime createTime;
}
