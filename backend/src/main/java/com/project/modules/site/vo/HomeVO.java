package com.project.modules.site.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class HomeVO {

    private List<SiteContentVO> hero;

    private List<SiteContentVO> intro;

    private List<SiteContentVO> workflowEntry;

    private List<SiteContentVO> toolRecommendEntry;

    private List<SiteContentVO> promptEntry;

    private List<SiteContentVO> caseEntry;

    private List<SiteContentVO> reviewEntry;

    private List<SiteContentVO> contact;

    private Map<String, List<SiteContentVO>> sections;

    private List<AwardRecordVO> awards;
}
