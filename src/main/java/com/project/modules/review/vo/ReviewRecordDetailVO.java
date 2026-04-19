package com.project.modules.review.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewRecordDetailVO {

    private Long id;

    private Long userId;

    private String title;

    private String code;

    private Long stageId;

    private Long toolId;

    private String projectName;

    private String summary;

    private String problemDesc;

    private String solutionDesc;

    private String reflection;

    private BigDecimal score;

    private LocalDate reviewDate;

    private Integer sortOrder;

    private Integer status;

    private ReviewStageVO stage;

    private ReviewToolVO tool;

    private List<ReviewAssetVO> assets;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
