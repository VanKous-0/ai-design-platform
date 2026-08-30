package com.project.modules.profile.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_recent_parameter")
public class UserRecentParameter {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String parameterType;

    private String parameterKey;

    private String parameterValue;

    private String source;

    private Integer useCount;

    private LocalDateTime lastUsedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
