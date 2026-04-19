package com.project.modules.site.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("award_record")
public class AwardRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String awardLevel;

    private String issuer;

    private LocalDate awardDate;

    private String summary;

    private String imageUrl;

    private String linkUrl;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
