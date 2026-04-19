package com.project.modules.review.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewStageVO {

    private Long id;

    private String name;

    private String code;
}
