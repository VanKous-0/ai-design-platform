package com.project.modules.site.service;

import com.project.modules.site.dto.AwardRecordCreateRequest;
import com.project.modules.site.dto.AwardRecordUpdateRequest;
import com.project.modules.site.dto.SiteContentCreateRequest;
import com.project.modules.site.dto.SiteContentUpdateRequest;
import com.project.modules.site.vo.AwardRecordVO;
import com.project.modules.site.vo.HomeVO;
import com.project.modules.site.vo.SiteContentVO;

import java.util.List;

public interface SiteService {

    HomeVO getHome();

    List<SiteContentVO> listContents(String sectionKey);

    SiteContentVO getContentDetail(Long id);

    SiteContentVO createContent(SiteContentCreateRequest request);

    SiteContentVO updateContent(Long id, SiteContentUpdateRequest request);

    void deleteContent(Long id);

    List<AwardRecordVO> listAwards();

    AwardRecordVO getAwardDetail(Long id);

    AwardRecordVO createAward(AwardRecordCreateRequest request);

    AwardRecordVO updateAward(Long id, AwardRecordUpdateRequest request);

    void deleteAward(Long id);
}
