package com.project.modules.site.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AwardRecordCreateRequest {

    @NotBlank(message = "title must not be blank")
    private String title;

    private String awardLevel;

    private String issuer;

    private LocalDate awardDate;

    private String summary;

    private String imageUrl;

    private String linkUrl;

    @Min(value = 0, message = "sortOrder must be greater than or equal to 0")
    private Integer sortOrder;

    private Integer status;
}
