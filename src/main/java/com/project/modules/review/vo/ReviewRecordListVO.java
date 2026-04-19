package com.project.modules.review.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewRecordListVO {

    private Long id;

    private Long userId;

    private String title;

    private String code;

    private Long stageId;

    private Long toolId;

    private String projectName;

    private String summary;

    private BigDecimal score;

    private LocalDate reviewDate;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
