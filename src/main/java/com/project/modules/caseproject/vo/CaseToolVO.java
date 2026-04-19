package com.project.modules.caseproject.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaseToolVO {

    private Long id;

    private String name;

    private String code;

    private String officialUrl;

    private String logoUrl;
}
