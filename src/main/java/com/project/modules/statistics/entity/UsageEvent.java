package com.project.modules.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("usage_event")
public class UsageEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String anonymousId;

    private String eventType;

    private String targetType;

    private Long targetId;

    private String pageUrl;

    private Integer stayDuration;

    private String inputSummary;

    private String extraJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
