package com.project.modules.caseproject.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.common.exception.BusinessException;
import com.project.common.result.PageResult;
import com.project.common.util.PageSupport;
import com.project.modules.caseproject.dto.CaseAssetCreateRequest;
import com.project.modules.caseproject.dto.CaseAssetUpdateRequest;
import com.project.modules.caseproject.dto.CaseAuditRequest;
import com.project.modules.caseproject.dto.CaseProjectCreateRequest;
import com.project.modules.caseproject.dto.CaseProjectUpdateRequest;
import com.project.modules.caseproject.dto.UserCaseAssetCreateRequest;
import com.project.modules.caseproject.dto.UserCaseCreateRequest;
import com.project.modules.caseproject.dto.UserCaseUpdateRequest;
import com.project.modules.caseproject.entity.CaseAsset;
import com.project.modules.caseproject.entity.CaseProject;
import com.project.modules.caseproject.entity.CaseToolUsage;
import com.project.modules.caseproject.mapper.CaseAssetMapper;
import com.project.modules.caseproject.mapper.CaseProjectMapper;
import com.project.modules.caseproject.mapper.CaseToolUsageMapper;
import com.project.modules.caseproject.service.CaseProjectService;
import com.project.modules.caseproject.vo.CaseAssetVO;
import com.project.modules.caseproject.vo.CaseAuditVO;
import com.project.modules.caseproject.vo.CaseProjectDetailVO;
import com.project.modules.caseproject.vo.CaseProjectListVO;
import com.project.modules.caseproject.vo.CaseStageVO;
import com.project.modules.caseproject.vo.CaseToolVO;
import com.project.modules.caseproject.vo.CaseToolUsageVO;
import com.project.modules.caseproject.vo.UserCaseDetailVO;
import com.project.modules.caseproject.vo.UserCaseVO;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.mapper.AiToolMapper;
import com.project.modules.workflow.entity.WorkflowStage;
import com.project.modules.workflow.mapper.WorkflowStageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class CaseProjectServiceImpl implements CaseProjectService {

    private static final int STATUS_ENABLED = 1;
    private static final int DEFAULT_STATUS = 1;
    private static final int DEFAULT_SORT_ORDER = 0;
    private static final String AUDIT_PENDING = "PENDING";
    private static final String AUDIT_APPROVED = "APPROVED";
    private static final String AUDIT_REJECTED = "REJECTED";

    private final CaseProjectMapper caseProjectMapper;
    private final CaseAssetMapper caseAssetMapper;
    private final CaseToolUsageMapper caseToolUsageMapper;
    private final WorkflowStageMapper workflowStageMapper;
    private final AiToolMapper aiToolMapper;

    public CaseProjectServiceImpl(
            CaseProjectMapper caseProjectMapper,
            CaseAssetMapper caseAssetMapper,
            CaseToolUsageMapper caseToolUsageMapper,
            WorkflowStageMapper workflowStageMapper,
            AiToolMapper aiToolMapper
    ) {
        this.caseProjectMapper = caseProjectMapper;
        this.caseAssetMapper = caseAssetMapper;
        this.caseToolUsageMapper = caseToolUsageMapper;
        this.workflowStageMapper = workflowStageMapper;
        this.aiToolMapper = aiToolMapper;
    }

    @Override
    public List<CaseProjectListVO> listCases(Long stageId, Long toolId, String keyword) {
        return caseProjectMapper.selectList(buildPublicCaseQuery(stageId, toolId, keyword))
                .stream()
                .map(this::toListVO)
                .toList();
    }

    @Override
    public PageResult<CaseProjectListVO> pageCases(
            Long stageId,
            Long toolId,
            String keyword,
            Long pageNum,
            Long pageSize
    ) {
        Page<CaseProject> page = PageSupport.page(pageNum, pageSize);
        Page<CaseProject> result = caseProjectMapper.selectPage(page, buildPublicCaseQuery(stageId, toolId, keyword));
        return PageResult.<CaseProjectListVO>builder()
                .records(result.getRecords().stream().map(this::toListVO).toList())
                .total(result.getTotal())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .pages(result.getPages())
                .build();
    }

    private LambdaQueryWrapper<CaseProject> buildPublicCaseQuery(Long stageId, Long toolId, String keyword) {
        LambdaQueryWrapper<CaseProject> queryWrapper = enabledCaseQuery()
                .orderByAsc(CaseProject::getSortOrder)
                .orderByAsc(CaseProject::getId);
        if (stageId != null) {
            ensureStageExists(stageId);
            queryWrapper.eq(CaseProject::getStageId, stageId);
        }
        if (toolId != null) {
            ensureToolExists(toolId);
            queryWrapper.eq(CaseProject::getToolId, toolId);
        }
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(CaseProject::getTitle, trimmedKeyword)
                    .or()
                    .like(CaseProject::getSummary, trimmedKeyword));
        }
        return queryWrapper;
    }

    @Override
    public CaseProjectDetailVO getCaseDetail(Long id) {
        CaseProject caseProject = getEnabledCaseEntity(id);
        return toDetailVO(caseProject);
    }

    @Override
    public List<CaseAssetVO> listAssets(Long caseId) {
        getEnabledCaseEntity(caseId);
        return caseAssetMapper.selectList(new LambdaQueryWrapper<CaseAsset>()
                        .eq(CaseAsset::getCaseId, caseId)
                        .orderByAsc(CaseAsset::getSortOrder)
                        .orderByAsc(CaseAsset::getId))
                .stream()
                .map(this::toAssetVO)
                .toList();
    }

    @Override
    public List<CaseProjectListVO> recommendCases(Long stageId, Long toolId) {
        if (stageId == null) {
            throw new BusinessException("阶段ID不能为空");
        }
        return listCases(stageId, toolId, null);
    }

    @Override
    public CaseProjectDetailVO createCase(CaseProjectCreateRequest request) {
        ensureStageExists(request.getStageId());
        if (request.getToolId() != null) {
            ensureToolExists(request.getToolId());
        }
        ensureCaseCodeUnique(request.getCode(), null);

        LocalDateTime now = LocalDateTime.now();
        CaseProject caseProject = new CaseProject();
        fillCase(caseProject, request.getTitle(), request.getCode(), request.getStageId(), request.getToolId(),
                request.getCoverUrl(), request.getSummary(), request.getContent(), request.getSourceDesc(),
                request.getAuthorName(), request.getSortOrder(), request.getStatus());
        caseProject.setAuditStatus(AUDIT_APPROVED);
        caseProject.setCreateTime(now);
        caseProject.setUpdateTime(now);
        caseProject.setIsDeleted(0);
        caseProjectMapper.insert(caseProject);
        return toDetailVO(caseProject);
    }

    @Override
    public CaseProjectDetailVO updateCase(Long id, CaseProjectUpdateRequest request) {
        CaseProject caseProject = getCaseEntity(id);
        ensureStageExists(request.getStageId());
        if (request.getToolId() != null) {
            ensureToolExists(request.getToolId());
        }
        ensureCaseCodeUnique(request.getCode(), id);

        fillCase(caseProject, request.getTitle(), request.getCode(), request.getStageId(), request.getToolId(),
                request.getCoverUrl(), request.getSummary(), request.getContent(), request.getSourceDesc(),
                request.getAuthorName(), request.getSortOrder(), request.getStatus());
        if (!StringUtils.hasText(caseProject.getAuditStatus())) {
            caseProject.setAuditStatus(AUDIT_APPROVED);
        }
        caseProject.setUpdateTime(LocalDateTime.now());
        caseProjectMapper.updateById(caseProject);
        return toDetailVO(caseProject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCase(Long id) {
        getCaseEntity(id);
        caseProjectMapper.deleteById(id);
        caseAssetMapper.delete(new LambdaQueryWrapper<CaseAsset>()
                .eq(CaseAsset::getCaseId, id));
        caseToolUsageMapper.delete(new LambdaQueryWrapper<CaseToolUsage>()
                .eq(CaseToolUsage::getCaseId, id));
    }

    @Override
    public CaseAssetVO createAsset(Long caseId, CaseAssetCreateRequest request) {
        getCaseEntity(caseId);
        validateAssetType(request.getAssetType());

        LocalDateTime now = LocalDateTime.now();
        CaseAsset asset = new CaseAsset();
        fillAsset(asset, caseId, request.getAssetType(), request.getAssetUrl(), request.getTitle(),
                request.getDescription(), request.getSortOrder());
        asset.setCreateTime(now);
        asset.setUpdateTime(now);
        asset.setIsDeleted(0);
        caseAssetMapper.insert(asset);
        return toAssetVO(asset);
    }

    @Override
    public CaseAssetVO updateAsset(Long id, CaseAssetUpdateRequest request) {
        CaseAsset asset = getAssetEntity(id);
        validateAssetType(request.getAssetType());

        fillAsset(asset, asset.getCaseId(), request.getAssetType(), request.getAssetUrl(), request.getTitle(),
                request.getDescription(), request.getSortOrder());
        asset.setUpdateTime(LocalDateTime.now());
        caseAssetMapper.updateById(asset);
        return toAssetVO(asset);
    }

    @Override
    public void deleteAsset(Long id) {
        getAssetEntity(id);
        caseAssetMapper.deleteById(id);
    }

    @Override
    public UserCaseDetailVO createUserCase(Long userId, UserCaseCreateRequest request) {
        ensureStageExists(request.getStageId());
        if (request.getToolId() != null) {
            ensureToolExists(request.getToolId());
        }
        ensureCaseCodeUnique(request.getCode(), null);

        LocalDateTime now = LocalDateTime.now();
        CaseProject caseProject = new CaseProject();
        fillCase(caseProject, request.getTitle(), request.getCode(), request.getStageId(), request.getToolId(),
                request.getCoverUrl(), request.getSummary(), request.getContent(), request.getSourceDesc(),
                request.getAuthorName(), request.getSortOrder(), DEFAULT_STATUS);
        caseProject.setSubmitUserId(userId);
        caseProject.setAuditStatus(AUDIT_PENDING);
        caseProject.setCreateTime(now);
        caseProject.setUpdateTime(now);
        caseProject.setIsDeleted(0);
        caseProjectMapper.insert(caseProject);
        return toUserDetailVO(caseProject);
    }

    @Override
    public List<UserCaseVO> listMyCases(Long userId) {
        return caseProjectMapper.selectList(new LambdaQueryWrapper<CaseProject>()
                        .eq(CaseProject::getSubmitUserId, userId)
                        .orderByDesc(CaseProject::getCreateTime)
                        .orderByDesc(CaseProject::getId))
                .stream()
                .map(this::toUserVO)
                .toList();
    }

    @Override
    public PageResult<UserCaseVO> pageMyCases(Long userId, Long pageNum, Long pageSize) {
        Page<CaseProject> page = PageSupport.page(pageNum, pageSize);
        Page<CaseProject> result = caseProjectMapper.selectPage(page, new LambdaQueryWrapper<CaseProject>()
                .eq(CaseProject::getSubmitUserId, userId)
                .orderByDesc(CaseProject::getCreateTime)
                .orderByDesc(CaseProject::getId));
        return PageResult.<UserCaseVO>builder()
                .records(result.getRecords().stream().map(this::toUserVO).toList())
                .total(result.getTotal())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .pages(result.getPages())
                .build();
    }

    @Override
    public UserCaseDetailVO getMyCaseDetail(Long userId, Long caseId) {
        return toUserDetailVO(getOwnedCaseEntity(userId, caseId));
    }

    @Override
    public UserCaseDetailVO updateMyCase(Long userId, Long caseId, UserCaseUpdateRequest request) {
        CaseProject caseProject = getOwnedCaseEntity(userId, caseId);
        ensureStageExists(request.getStageId());
        if (request.getToolId() != null) {
            ensureToolExists(request.getToolId());
        }
        ensureCaseCodeUnique(request.getCode(), caseId);

        fillCase(caseProject, request.getTitle(), request.getCode(), request.getStageId(), request.getToolId(),
                request.getCoverUrl(), request.getSummary(), request.getContent(), request.getSourceDesc(),
                request.getAuthorName(), request.getSortOrder(), DEFAULT_STATUS);
        caseProject.setAuditStatus(AUDIT_PENDING);
        caseProject.setAuditComment(null);
        caseProject.setAuditTime(null);
        caseProject.setAuditorId(null);
        caseProject.setUpdateTime(LocalDateTime.now());
        caseProjectMapper.updateById(caseProject);
        return toUserDetailVO(caseProject);
    }

    @Override
    public CaseAssetVO createMyCaseAsset(Long userId, Long caseId, UserCaseAssetCreateRequest request) {
        getOwnedCaseEntity(userId, caseId);
        validateAssetType(request.getAssetType());

        LocalDateTime now = LocalDateTime.now();
        CaseAsset asset = new CaseAsset();
        fillAsset(asset, caseId, request.getAssetType(), request.getAssetUrl(), request.getTitle(),
                request.getDescription(), request.getSortOrder());
        asset.setCreateTime(now);
        asset.setUpdateTime(now);
        asset.setIsDeleted(0);
        caseAssetMapper.insert(asset);
        return toAssetVO(asset);
    }

    @Override
    public void deleteMyCaseAsset(Long userId, Long assetId) {
        CaseAsset asset = getAssetEntity(assetId);
        getOwnedCaseEntity(userId, asset.getCaseId());
        caseAssetMapper.deleteById(assetId);
    }

    @Override
    public List<CaseAuditVO> listPendingCases() {
        return listAuditCases(AUDIT_PENDING, null, null);
    }

    @Override
    public List<CaseAuditVO> listAuditCases(String auditStatus, String keyword, Long submitUserId) {
        return caseProjectMapper.selectList(buildAuditCaseQuery(auditStatus, keyword, submitUserId))
                .stream()
                .map(this::toAuditVO)
                .toList();
    }

    @Override
    public PageResult<CaseAuditVO> pageAuditCases(
            String auditStatus,
            String keyword,
            Long submitUserId,
            Long pageNum,
            Long pageSize
    ) {
        Page<CaseProject> page = PageSupport.page(pageNum, pageSize);
        Page<CaseProject> result = caseProjectMapper.selectPage(
                page,
                buildAuditCaseQuery(auditStatus, keyword, submitUserId)
        );
        return PageResult.<CaseAuditVO>builder()
                .records(result.getRecords().stream().map(this::toAuditVO).toList())
                .total(result.getTotal())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .pages(result.getPages())
                .build();
    }

    private LambdaQueryWrapper<CaseProject> buildAuditCaseQuery(
            String auditStatus,
            String keyword,
            Long submitUserId
    ) {
        LambdaQueryWrapper<CaseProject> query = new LambdaQueryWrapper<CaseProject>()
                .orderByDesc(CaseProject::getCreateTime)
                .orderByDesc(CaseProject::getId);
        if (StringUtils.hasText(auditStatus)) {
            validateAuditStatus(auditStatus);
            query.eq(CaseProject::getAuditStatus, auditStatus.trim());
        }
        if (submitUserId != null) {
            query.eq(CaseProject::getSubmitUserId, submitUserId);
        }
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            query.and(wrapper -> wrapper
                    .like(CaseProject::getTitle, trimmedKeyword)
                    .or()
                    .like(CaseProject::getSummary, trimmedKeyword));
        }
        return query;
    }

    @Override
    public CaseAuditVO approveCase(Long adminUserId, Long caseId, CaseAuditRequest request) {
        return auditCase(adminUserId, caseId, request, AUDIT_APPROVED);
    }

    @Override
    public CaseAuditVO rejectCase(Long adminUserId, Long caseId, CaseAuditRequest request) {
        return auditCase(adminUserId, caseId, request, AUDIT_REJECTED);
    }

    private LambdaQueryWrapper<CaseProject> enabledCaseQuery() {
        return new LambdaQueryWrapper<CaseProject>()
                .eq(CaseProject::getStatus, STATUS_ENABLED)
                .eq(CaseProject::getAuditStatus, AUDIT_APPROVED);
    }

    private void fillCase(
            CaseProject caseProject,
            String title,
            String code,
            Long stageId,
            Long toolId,
            String coverUrl,
            String summary,
            String content,
            String sourceDesc,
            String authorName,
            Integer sortOrder,
            Integer status
    ) {
        caseProject.setTitle(title);
        caseProject.setCode(code);
        caseProject.setStageId(stageId);
        caseProject.setToolId(toolId);
        caseProject.setCoverUrl(coverUrl);
        caseProject.setSummary(summary);
        caseProject.setContent(content);
        caseProject.setSourceDesc(sourceDesc);
        caseProject.setAuthorName(authorName);
        caseProject.setSortOrder(defaultIfNull(sortOrder, DEFAULT_SORT_ORDER));
        caseProject.setStatus(defaultIfNull(status, DEFAULT_STATUS));
    }

    private void fillAsset(
            CaseAsset asset,
            Long caseId,
            String assetType,
            String assetUrl,
            String title,
            String description,
            Integer sortOrder
    ) {
        asset.setCaseId(caseId);
        asset.setAssetType(assetType);
        asset.setAssetUrl(assetUrl);
        asset.setTitle(title);
        asset.setDescription(description);
        asset.setSortOrder(defaultIfNull(sortOrder, DEFAULT_SORT_ORDER));
    }

    private CaseProject getCaseEntity(Long id) {
        CaseProject caseProject = caseProjectMapper.selectById(id);
        if (caseProject == null) {
            throw new BusinessException("案例不存在");
        }
        return caseProject;
    }

    private CaseProject getEnabledCaseEntity(Long id) {
        CaseProject caseProject = caseProjectMapper.selectOne(enabledCaseQuery()
                .eq(CaseProject::getId, id)
                .last("limit 1"));
        if (caseProject == null) {
            throw new BusinessException("案例不存在或未启用");
        }
        return caseProject;
    }

    private CaseAsset getAssetEntity(Long id) {
        CaseAsset asset = caseAssetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("案例资源不存在");
        }
        return asset;
    }

    private CaseProject getOwnedCaseEntity(Long userId, Long caseId) {
        CaseProject caseProject = caseProjectMapper.selectOne(new LambdaQueryWrapper<CaseProject>()
                .eq(CaseProject::getId, caseId)
                .eq(CaseProject::getSubmitUserId, userId)
                .last("limit 1"));
        if (caseProject == null) {
            throw new BusinessException("案例不存在或无权操作");
        }
        return caseProject;
    }

    private CaseAuditVO auditCase(Long adminUserId, Long caseId, CaseAuditRequest request, String auditStatus) {
        CaseProject caseProject = getCaseEntity(caseId);
        LocalDateTime now = LocalDateTime.now();
        caseProject.setAuditStatus(auditStatus);
        caseProject.setAuditComment(request == null ? null : request.getAuditComment());
        caseProject.setAuditTime(now);
        caseProject.setAuditorId(adminUserId);
        caseProject.setUpdateTime(now);
        caseProjectMapper.updateById(caseProject);
        return toAuditVO(caseProject);
    }

    private void validateAuditStatus(String auditStatus) {
        String trimmed = auditStatus.trim();
        if (!AUDIT_PENDING.equals(trimmed) && !AUDIT_APPROVED.equals(trimmed) && !AUDIT_REJECTED.equals(trimmed)) {
            throw new BusinessException("审核状态只支持 PENDING / APPROVED / REJECTED");
        }
    }

    private void ensureCaseCodeUnique(String code, Long excludeId) {
        CaseProject existing = caseProjectMapper.selectOne(new LambdaQueryWrapper<CaseProject>()
                .eq(CaseProject::getCode, code)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new BusinessException("案例编码已存在");
        }
    }

    private void ensureStageExists(Long stageId) {
        WorkflowStage stage = workflowStageMapper.selectById(stageId);
        if (stage == null || !Objects.equals(stage.getStatus(), STATUS_ENABLED)) {
            throw new BusinessException("工作流阶段不存在或未启用");
        }
    }

    private void ensureToolExists(Long toolId) {
        AiTool tool = aiToolMapper.selectById(toolId);
        if (tool == null || !Objects.equals(tool.getStatus(), STATUS_ENABLED)) {
            throw new BusinessException("AI工具不存在或未启用");
        }
    }

    private void validateAssetType(String assetType) {
        if (!"image".equals(assetType) && !"file".equals(assetType)) {
            throw new BusinessException("资源类型只支持 image 或 file");
        }
    }

    private CaseProjectListVO toListVO(CaseProject caseProject) {
        return CaseProjectListVO.builder()
                .id(caseProject.getId())
                .title(caseProject.getTitle())
                .code(caseProject.getCode())
                .stageId(caseProject.getStageId())
                .toolId(caseProject.getToolId())
                .coverUrl(caseProject.getCoverUrl())
                .summary(caseProject.getSummary())
                .sourceDesc(caseProject.getSourceDesc())
                .authorName(caseProject.getAuthorName())
                .sortOrder(caseProject.getSortOrder())
                .status(caseProject.getStatus())
                .createTime(caseProject.getCreateTime())
                .updateTime(caseProject.getUpdateTime())
                .build();
    }

    private CaseProjectDetailVO toDetailVO(CaseProject caseProject) {
        return CaseProjectDetailVO.builder()
                .id(caseProject.getId())
                .title(caseProject.getTitle())
                .code(caseProject.getCode())
                .stageId(caseProject.getStageId())
                .toolId(caseProject.getToolId())
                .coverUrl(caseProject.getCoverUrl())
                .summary(caseProject.getSummary())
                .content(caseProject.getContent())
                .sourceDesc(caseProject.getSourceDesc())
                .authorName(caseProject.getAuthorName())
                .sortOrder(caseProject.getSortOrder())
                .status(caseProject.getStatus())
                .stage(loadStage(caseProject.getStageId()))
                .tool(loadTool(caseProject.getToolId()))
                .assets(listAssetsWithoutCaseCheck(caseProject.getId()))
                .toolUsages(listToolUsages(caseProject.getId()))
                .createTime(caseProject.getCreateTime())
                .updateTime(caseProject.getUpdateTime())
                .build();
    }

    private UserCaseVO toUserVO(CaseProject caseProject) {
        return UserCaseVO.builder()
                .id(caseProject.getId())
                .title(caseProject.getTitle())
                .code(caseProject.getCode())
                .stageId(caseProject.getStageId())
                .toolId(caseProject.getToolId())
                .coverUrl(caseProject.getCoverUrl())
                .summary(caseProject.getSummary())
                .sourceDesc(caseProject.getSourceDesc())
                .authorName(caseProject.getAuthorName())
                .sortOrder(caseProject.getSortOrder())
                .status(caseProject.getStatus())
                .auditStatus(caseProject.getAuditStatus())
                .auditComment(caseProject.getAuditComment())
                .auditTime(caseProject.getAuditTime())
                .createTime(caseProject.getCreateTime())
                .updateTime(caseProject.getUpdateTime())
                .build();
    }

    private UserCaseDetailVO toUserDetailVO(CaseProject caseProject) {
        return UserCaseDetailVO.builder()
                .id(caseProject.getId())
                .title(caseProject.getTitle())
                .code(caseProject.getCode())
                .stageId(caseProject.getStageId())
                .toolId(caseProject.getToolId())
                .coverUrl(caseProject.getCoverUrl())
                .summary(caseProject.getSummary())
                .content(caseProject.getContent())
                .sourceDesc(caseProject.getSourceDesc())
                .authorName(caseProject.getAuthorName())
                .sortOrder(caseProject.getSortOrder())
                .status(caseProject.getStatus())
                .auditStatus(caseProject.getAuditStatus())
                .auditComment(caseProject.getAuditComment())
                .auditTime(caseProject.getAuditTime())
                .stage(loadStage(caseProject.getStageId()))
                .tool(loadTool(caseProject.getToolId()))
                .assets(listAssetsWithoutCaseCheck(caseProject.getId()))
                .createTime(caseProject.getCreateTime())
                .updateTime(caseProject.getUpdateTime())
                .build();
    }

    private CaseAuditVO toAuditVO(CaseProject caseProject) {
        return CaseAuditVO.builder()
                .id(caseProject.getId())
                .title(caseProject.getTitle())
                .code(caseProject.getCode())
                .submitUserId(caseProject.getSubmitUserId())
                .summary(caseProject.getSummary())
                .auditStatus(caseProject.getAuditStatus())
                .auditComment(caseProject.getAuditComment())
                .auditTime(caseProject.getAuditTime())
                .auditorId(caseProject.getAuditorId())
                .status(caseProject.getStatus())
                .createTime(caseProject.getCreateTime())
                .updateTime(caseProject.getUpdateTime())
                .build();
    }

    private List<CaseAssetVO> listAssetsWithoutCaseCheck(Long caseId) {
        return caseAssetMapper.selectList(new LambdaQueryWrapper<CaseAsset>()
                        .eq(CaseAsset::getCaseId, caseId)
                        .orderByAsc(CaseAsset::getSortOrder)
                        .orderByAsc(CaseAsset::getId))
                .stream()
                .map(this::toAssetVO)
                .toList();
    }

    private CaseAssetVO toAssetVO(CaseAsset asset) {
        return CaseAssetVO.builder()
                .id(asset.getId())
                .caseId(asset.getCaseId())
                .assetType(asset.getAssetType())
                .assetUrl(asset.getAssetUrl())
                .title(asset.getTitle())
                .description(asset.getDescription())
                .sortOrder(asset.getSortOrder())
                .createTime(asset.getCreateTime())
                .updateTime(asset.getUpdateTime())
                .build();
    }

    private List<CaseToolUsageVO> listToolUsages(Long caseId) {
        return caseToolUsageMapper.selectList(new LambdaQueryWrapper<CaseToolUsage>()
                        .eq(CaseToolUsage::getCaseId, caseId)
                        .orderByAsc(CaseToolUsage::getSortOrder)
                        .orderByAsc(CaseToolUsage::getId))
                .stream()
                .map(usage -> CaseToolUsageVO.builder()
                        .id(usage.getId())
                        .toolId(usage.getToolId())
                        .toolName(usage.getToolName())
                        .toolCode(usage.getToolCode())
                        .toolType(usage.getToolType())
                        .usageStage(usage.getUsageStage())
                        .usageDesc(usage.getUsageDesc())
                        .sortOrder(usage.getSortOrder())
                        .build())
                .toList();
    }

    private CaseStageVO loadStage(Long stageId) {
        WorkflowStage stage = workflowStageMapper.selectById(stageId);
        if (stage == null) {
            return null;
        }
        return CaseStageVO.builder()
                .id(stage.getId())
                .name(stage.getName())
                .code(stage.getCode())
                .build();
    }

    private CaseToolVO loadTool(Long toolId) {
        if (toolId == null) {
            return null;
        }
        AiTool tool = aiToolMapper.selectById(toolId);
        if (tool == null) {
            return null;
        }
        return CaseToolVO.builder()
                .id(tool.getId())
                .name(tool.getName())
                .code(tool.getCode())
                .officialUrl(tool.getOfficialUrl())
                .logoUrl(tool.getLogoUrl())
                .build();
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
