package com.project.modules.tool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_tool")
public class AiTool {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String officialUrl;

    private String logoUrl;

    private String description;

    private String priceDesc;

    private String versionDesc;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
