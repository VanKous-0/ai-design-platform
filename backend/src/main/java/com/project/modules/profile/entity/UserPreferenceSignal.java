package com.project.modules.profile.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_preference_signal")
public class UserPreferenceSignal {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String preferenceKey;

    private String preferenceValue;

    private String sentiment;

    private String scope;

    private String source;

    private BigDecimal confidence;

    private Integer evidenceCount;

    private String evidenceSummary;

    private LocalDateTime lastObservedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
