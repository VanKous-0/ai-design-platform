package com.project.modules.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("review_record")
public class ReviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String code;

    private Long stageId;

    private Long toolId;

    private Long caseId;

    private String projectName;

    private String summary;

    private String problemDesc;

    private String solutionDesc;

    private String reflection;

    private BigDecimal score;

    private LocalDate reviewDate;

    private String sourceType;

    private String sourceFile;

    private String sourcePage;

    private String sourceDesc;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
