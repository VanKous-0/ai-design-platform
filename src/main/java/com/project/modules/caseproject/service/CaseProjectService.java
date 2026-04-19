package com.project.modules.caseproject.service;

import com.project.modules.caseproject.dto.CaseAssetCreateRequest;
import com.project.modules.caseproject.dto.CaseAssetUpdateRequest;
import com.project.modules.caseproject.dto.CaseProjectCreateRequest;
import com.project.modules.caseproject.dto.CaseProjectUpdateRequest;
import com.project.modules.caseproject.vo.CaseAssetVO;
import com.project.modules.caseproject.vo.CaseProjectDetailVO;
import com.project.modules.caseproject.vo.CaseProjectListVO;

import java.util.List;

public interface CaseProjectService {

    List<CaseProjectListVO> listCases(Long stageId, Long toolId, String keyword);

    CaseProjectDetailVO getCaseDetail(Long id);

    List<CaseAssetVO> listAssets(Long caseId);

    List<CaseProjectListVO> recommendCases(Long stageId, Long toolId);

    CaseProjectDetailVO createCase(CaseProjectCreateRequest request);

    CaseProjectDetailVO updateCase(Long id, CaseProjectUpdateRequest request);

    void deleteCase(Long id);

    CaseAssetVO createAsset(Long caseId, CaseAssetCreateRequest request);

    CaseAssetVO updateAsset(Long id, CaseAssetUpdateRequest request);

    void deleteAsset(Long id);
}
