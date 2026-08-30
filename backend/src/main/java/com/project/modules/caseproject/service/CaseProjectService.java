package com.project.modules.caseproject.service;

import com.project.common.result.PageResult;
import com.project.modules.caseproject.dto.CaseAssetCreateRequest;
import com.project.modules.caseproject.dto.CaseAssetUpdateRequest;
import com.project.modules.caseproject.dto.CaseAuditRequest;
import com.project.modules.caseproject.dto.CaseProjectCreateRequest;
import com.project.modules.caseproject.dto.CaseProjectUpdateRequest;
import com.project.modules.caseproject.dto.UserCaseAssetCreateRequest;
import com.project.modules.caseproject.dto.UserCaseCreateRequest;
import com.project.modules.caseproject.dto.UserCaseUpdateRequest;
import com.project.modules.caseproject.vo.CaseAssetVO;
import com.project.modules.caseproject.vo.CaseAuditVO;
import com.project.modules.caseproject.vo.CaseProjectDetailVO;
import com.project.modules.caseproject.vo.CaseProjectListVO;
import com.project.modules.caseproject.vo.UserCaseDetailVO;
import com.project.modules.caseproject.vo.UserCaseVO;

import java.util.List;

public interface CaseProjectService {

    List<CaseProjectListVO> listCases(Long stageId, Long toolId, String keyword);

    PageResult<CaseProjectListVO> pageCases(Long stageId, Long toolId, String keyword, Long pageNum, Long pageSize);

    CaseProjectDetailVO getCaseDetail(Long id);

    List<CaseAssetVO> listAssets(Long caseId);

    List<CaseProjectListVO> recommendCases(Long stageId, Long toolId);

    CaseProjectDetailVO createCase(CaseProjectCreateRequest request);

    CaseProjectDetailVO updateCase(Long id, CaseProjectUpdateRequest request);

    void deleteCase(Long id);

    CaseAssetVO createAsset(Long caseId, CaseAssetCreateRequest request);

    CaseAssetVO updateAsset(Long id, CaseAssetUpdateRequest request);

    void deleteAsset(Long id);

    UserCaseDetailVO createUserCase(Long userId, UserCaseCreateRequest request);

    List<UserCaseVO> listMyCases(Long userId);

    PageResult<UserCaseVO> pageMyCases(Long userId, Long pageNum, Long pageSize);

    UserCaseDetailVO getMyCaseDetail(Long userId, Long caseId);

    UserCaseDetailVO updateMyCase(Long userId, Long caseId, UserCaseUpdateRequest request);

    CaseAssetVO createMyCaseAsset(Long userId, Long caseId, UserCaseAssetCreateRequest request);

    void deleteMyCaseAsset(Long userId, Long assetId);

    List<CaseAuditVO> listPendingCases();

    List<CaseAuditVO> listAuditCases(String auditStatus, String keyword, Long submitUserId);

    PageResult<CaseAuditVO> pageAuditCases(
            String auditStatus,
            String keyword,
            Long submitUserId,
            Long pageNum,
            Long pageSize
    );

    CaseAuditVO approveCase(Long adminUserId, Long caseId, CaseAuditRequest request);

    CaseAuditVO rejectCase(Long adminUserId, Long caseId, CaseAuditRequest request);
}
