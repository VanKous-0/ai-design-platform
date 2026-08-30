package com.project.modules.site.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("site_content")
public class SiteContent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sectionKey;

    private String title;

    private String subtitle;

    private String content;

    private String imageUrl;

    private String linkUrl;

    private String extraJson;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
