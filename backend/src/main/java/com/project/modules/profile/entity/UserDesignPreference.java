package com.project.modules.profile.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_design_preference")
public class UserDesignPreference {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String preferredProjectType;

    private String preferredStyle;

    private String preferredSiteScale;

    private String preferredTargetUser;

    private Long defaultToolId;

    private String extraJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
