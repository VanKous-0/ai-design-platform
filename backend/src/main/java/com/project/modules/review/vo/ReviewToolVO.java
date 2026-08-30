package com.project.modules.review.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewToolVO {

    private Long id;

    private String name;

    private String code;

    private String officialUrl;

    private String logoUrl;
}
