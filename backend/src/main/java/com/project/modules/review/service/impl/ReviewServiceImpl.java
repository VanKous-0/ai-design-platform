package com.project.modules.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.common.exception.BusinessException;
import com.project.common.result.PageResult;
import com.project.common.util.PageSupport;
import com.project.modules.review.dto.ReviewAssetCreateRequest;
import com.project.modules.review.dto.ReviewAssetUpdateRequest;
import com.project.modules.review.dto.ReviewRecordCreateRequest;
import com.project.modules.review.dto.ReviewRecordUpdateRequest;
import com.project.modules.review.entity.ReviewAsset;
import com.project.modules.review.entity.ReviewRecord;
import com.project.modules.review.mapper.ReviewAssetMapper;
import com.project.modules.review.mapper.ReviewRecordMapper;
import com.project.modules.review.service.ReviewService;
import com.project.modules.review.vo.ReviewAssetVO;
import com.project.modules.review.vo.ReviewRecordDetailVO;
import com.project.modules.review.vo.ReviewRecordListVO;
import com.project.modules.review.vo.ReviewStageVO;
import com.project.modules.review.vo.ReviewToolVO;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.mapper.AiToolMapper;
import com.project.modules.user.entity.SysUser;
import com.project.modules.user.mapper.SysUserMapper;
import com.project.modules.workflow.entity.WorkflowStage;
import com.project.modules.workflow.mapper.WorkflowStageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final int STATUS_ENABLED = 1;
    private static final int DEFAULT_STATUS = 1;
    private static final int DEFAULT_SORT_ORDER = 0;
    private static final String DEFAULT_SOURCE_TYPE = "MANUAL";

    private final ReviewRecordMapper reviewRecordMapper;
    private final ReviewAssetMapper reviewAssetMapper;
    private final SysUserMapper sysUserMapper;
    private final WorkflowStageMapper workflowStageMapper;
    private final AiToolMapper aiToolMapper;

    public ReviewServiceImpl(
            ReviewRecordMapper reviewRecordMapper,
            ReviewAssetMapper reviewAssetMapper,
            SysUserMapper sysUserMapper,
            WorkflowStageMapper workflowStageMapper,
            AiToolMapper aiToolMapper
    ) {
        this.reviewRecordMapper = reviewRecordMapper;
        this.reviewAssetMapper = reviewAssetMapper;
        this.sysUserMapper = sysUserMapper;
        this.workflowStageMapper = workflowStageMapper;
        this.aiToolMapper = aiToolMapper;
    }

    @Override
    public List<ReviewRecordListVO> listReviews(Long stageId, Long toolId, String keyword) {
        return reviewRecordMapper.selectList(buildReviewQuery(stageId, toolId, keyword))
                .stream()
                .map(this::toListVO)
                .toList();
    }

    @Override
    public PageResult<ReviewRecordListVO> pageReviews(
            Long stageId,
            Long toolId,
            String keyword,
            Long pageNum,
            Long pageSize
    ) {
        Page<ReviewRecord> page = PageSupport.page(pageNum, pageSize);
        Page<ReviewRecord> result = reviewRecordMapper.selectPage(
                page,
                buildReviewQuery(stageId, toolId, keyword)
        );
        return PageResult.<ReviewRecordListVO>builder()
                .records(result.getRecords().stream().map(this::toListVO).toList())
                .total(result.getTotal())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .pages(result.getPages())
                .build();
    }

    private LambdaQueryWrapper<ReviewRecord> buildReviewQuery(Long stageId, Long toolId, String keyword) {
        LambdaQueryWrapper<ReviewRecord> queryWrapper = enabledReviewQuery()
                .orderByAsc(ReviewRecord::getSortOrder)
                .orderByAsc(ReviewRecord::getId);
        if (stageId != null) {
            ensureStageExists(stageId);
            queryWrapper.eq(ReviewRecord::getStageId, stageId);
        }
        if (toolId != null) {
            ensureToolExists(toolId);
            queryWrapper.eq(ReviewRecord::getToolId, toolId);
        }
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(ReviewRecord::getTitle, trimmedKeyword)
                    .or()
                    .like(ReviewRecord::getProjectName, trimmedKeyword)
                    .or()
                    .like(ReviewRecord::getSummary, trimmedKeyword));
        }
        return queryWrapper;
    }

    @Override
    public ReviewRecordDetailVO getReviewDetail(Long id) {
        ReviewRecord review = getEnabledReviewEntity(id);
        return toDetailVO(review);
    }

    @Override
    public List<ReviewAssetVO> listAssets(Long reviewId) {
        getEnabledReviewEntity(reviewId);
        return reviewAssetMapper.selectList(new LambdaQueryWrapper<ReviewAsset>()
                        .eq(ReviewAsset::getReviewId, reviewId)
                        .orderByAsc(ReviewAsset::getSortOrder)
                        .orderByAsc(ReviewAsset::getId))
                .stream()
                .map(this::toAssetVO)
                .toList();
    }

    @Override
    public List<ReviewRecordListVO> recommendReviews(Long stageId, Long toolId) {
        if (stageId == null) {
            throw new BusinessException("阶段ID不能为空");
        }
        return listReviews(stageId, toolId, null);
    }

    @Override
    public ReviewRecordDetailVO createReview(ReviewRecordCreateRequest request) {
        ensureUserExists(request.getUserId());
        ensureStageExists(request.getStageId());
        if (request.getToolId() != null) {
            ensureToolExists(request.getToolId());
        }
        ensureReviewCodeUnique(request.getCode(), null);

        LocalDateTime now = LocalDateTime.now();
        ReviewRecord review = new ReviewRecord();
        fillReview(review, request);
        review.setCreateTime(now);
        review.setUpdateTime(now);
        review.setIsDeleted(0);
        reviewRecordMapper.insert(review);
        return toDetailVO(review);
    }

    @Override
    public ReviewRecordDetailVO updateReview(Long id, ReviewRecordUpdateRequest request) {
        ReviewRecord review = getReviewEntity(id);
        ensureUserExists(request.getUserId());
        ensureStageExists(request.getStageId());
        if (request.getToolId() != null) {
            ensureToolExists(request.getToolId());
        }
        ensureReviewCodeUnique(request.getCode(), id);

        fillReview(review, request);
        review.setUpdateTime(LocalDateTime.now());
        reviewRecordMapper.updateById(review);
        return toDetailVO(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long id) {
        getReviewEntity(id);
        reviewRecordMapper.deleteById(id);
        reviewAssetMapper.delete(new LambdaQueryWrapper<ReviewAsset>()
                .eq(ReviewAsset::getReviewId, id));
    }

    @Override
    public ReviewAssetVO createAsset(Long reviewId, ReviewAssetCreateRequest request) {
        getReviewEntity(reviewId);
        validateAssetType(request.getAssetType());

        LocalDateTime now = LocalDateTime.now();
        ReviewAsset asset = new ReviewAsset();
        fillAsset(asset, reviewId, request.getAssetType(), request.getAssetUrl(), request.getTitle(),
                request.getDescription(), request.getSortOrder());
        asset.setCreateTime(now);
        asset.setUpdateTime(now);
        asset.setIsDeleted(0);
        reviewAssetMapper.insert(asset);
        return toAssetVO(asset);
    }

    @Override
    public ReviewAssetVO updateAsset(Long id, ReviewAssetUpdateRequest request) {
        ReviewAsset asset = getAssetEntity(id);
        validateAssetType(request.getAssetType());
        fillAsset(asset, asset.getReviewId(), request.getAssetType(), request.getAssetUrl(), request.getTitle(),
                request.getDescription(), request.getSortOrder());
        asset.setUpdateTime(LocalDateTime.now());
        reviewAssetMapper.updateById(asset);
        return toAssetVO(asset);
    }

    @Override
    public void deleteAsset(Long id) {
        getAssetEntity(id);
        reviewAssetMapper.deleteById(id);
    }

    private LambdaQueryWrapper<ReviewRecord> enabledReviewQuery() {
        return new LambdaQueryWrapper<ReviewRecord>()
                .eq(ReviewRecord::getStatus, STATUS_ENABLED)
                .ne(ReviewRecord::getSourceType, "DEMO");
    }

    private void fillReview(ReviewRecord review, ReviewRecordCreateRequest request) {
        review.setUserId(request.getUserId());
        review.setTitle(request.getTitle());
        review.setCode(request.getCode());
        review.setStageId(request.getStageId());
        review.setToolId(request.getToolId());
        review.setProjectName(request.getProjectName());
        review.setSummary(request.getSummary());
        review.setProblemDesc(request.getProblemDesc());
        review.setSolutionDesc(request.getSolutionDesc());
        review.setReflection(request.getReflection());
        review.setScore(request.getScore());
        review.setReviewDate(request.getReviewDate());
        review.setSourceType(DEFAULT_SOURCE_TYPE);
        review.setSortOrder(defaultIfNull(request.getSortOrder(), DEFAULT_SORT_ORDER));
        review.setStatus(defaultIfNull(request.getStatus(), DEFAULT_STATUS));
    }

    private void fillReview(ReviewRecord review, ReviewRecordUpdateRequest request) {
        review.setUserId(request.getUserId());
        review.setTitle(request.getTitle());
        review.setCode(request.getCode());
        review.setStageId(request.getStageId());
        review.setToolId(request.getToolId());
        review.setProjectName(request.getProjectName());
        review.setSummary(request.getSummary());
        review.setProblemDesc(request.getProblemDesc());
        review.setSolutionDesc(request.getSolutionDesc());
        review.setReflection(request.getReflection());
        review.setScore(request.getScore());
        review.setReviewDate(request.getReviewDate());
        if (!StringUtils.hasText(review.getSourceType())) {
            review.setSourceType(DEFAULT_SOURCE_TYPE);
        }
        review.setSortOrder(defaultIfNull(request.getSortOrder(), DEFAULT_SORT_ORDER));
        review.setStatus(defaultIfNull(request.getStatus(), DEFAULT_STATUS));
    }

    private void fillAsset(
            ReviewAsset asset,
            Long reviewId,
            String assetType,
            String assetUrl,
            String title,
            String description,
            Integer sortOrder
    ) {
        asset.setReviewId(reviewId);
        asset.setAssetType(assetType);
        asset.setAssetUrl(assetUrl);
        asset.setTitle(title);
        asset.setDescription(description);
        asset.setSortOrder(defaultIfNull(sortOrder, DEFAULT_SORT_ORDER));
    }

    private ReviewRecord getReviewEntity(Long id) {
        ReviewRecord review = reviewRecordMapper.selectById(id);
        if (review == null) {
            throw new BusinessException("复盘记录不存在");
        }
        return review;
    }

    private ReviewRecord getEnabledReviewEntity(Long id) {
        ReviewRecord review = reviewRecordMapper.selectOne(enabledReviewQuery()
                .eq(ReviewRecord::getId, id)
                .last("limit 1"));
        if (review == null) {
            throw new BusinessException("复盘记录不存在或未启用");
        }
        return review;
    }

    private ReviewAsset getAssetEntity(Long id) {
        ReviewAsset asset = reviewAssetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("复盘附件不存在");
        }
        return asset;
    }

    private void ensureReviewCodeUnique(String code, Long excludeId) {
        ReviewRecord existing = reviewRecordMapper.selectOne(new LambdaQueryWrapper<ReviewRecord>()
                .eq(ReviewRecord::getCode, code)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new BusinessException("复盘编码已存在");
        }
    }

    private void ensureUserExists(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Objects.equals(user.getStatus(), STATUS_ENABLED)) {
            throw new BusinessException("用户不存在或已禁用");
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
            throw new BusinessException("附件类型只支持 image 或 file");
        }
    }

    private ReviewRecordListVO toListVO(ReviewRecord review) {
        return ReviewRecordListVO.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .title(review.getTitle())
                .code(review.getCode())
                .stageId(review.getStageId())
                .toolId(review.getToolId())
                .caseId(review.getCaseId())
                .projectName(review.getProjectName())
                .summary(review.getSummary())
                .score(review.getScore())
                .reviewDate(review.getReviewDate())
                .sourceType(review.getSourceType())
                .sourceFile(review.getSourceFile())
                .sourcePage(review.getSourcePage())
                .sortOrder(review.getSortOrder())
                .status(review.getStatus())
                .createTime(review.getCreateTime())
                .updateTime(review.getUpdateTime())
                .build();
    }

    private ReviewRecordDetailVO toDetailVO(ReviewRecord review) {
        return ReviewRecordDetailVO.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .title(review.getTitle())
                .code(review.getCode())
                .stageId(review.getStageId())
                .toolId(review.getToolId())
                .caseId(review.getCaseId())
                .projectName(review.getProjectName())
                .summary(review.getSummary())
                .problemDesc(review.getProblemDesc())
                .solutionDesc(review.getSolutionDesc())
                .reflection(review.getReflection())
                .score(review.getScore())
                .reviewDate(review.getReviewDate())
                .sourceType(review.getSourceType())
                .sourceFile(review.getSourceFile())
                .sourcePage(review.getSourcePage())
                .sourceDesc(review.getSourceDesc())
                .sortOrder(review.getSortOrder())
                .status(review.getStatus())
                .stage(loadStage(review.getStageId()))
                .tool(loadTool(review.getToolId()))
                .assets(listAssetsWithoutReviewCheck(review.getId()))
                .createTime(review.getCreateTime())
                .updateTime(review.getUpdateTime())
                .build();
    }

    private List<ReviewAssetVO> listAssetsWithoutReviewCheck(Long reviewId) {
        return reviewAssetMapper.selectList(new LambdaQueryWrapper<ReviewAsset>()
                        .eq(ReviewAsset::getReviewId, reviewId)
                        .orderByAsc(ReviewAsset::getSortOrder)
                        .orderByAsc(ReviewAsset::getId))
                .stream()
                .map(this::toAssetVO)
                .toList();
    }

    private ReviewAssetVO toAssetVO(ReviewAsset asset) {
        return ReviewAssetVO.builder()
                .id(asset.getId())
                .reviewId(asset.getReviewId())
                .assetType(asset.getAssetType())
                .assetUrl(asset.getAssetUrl())
                .title(asset.getTitle())
                .description(asset.getDescription())
                .sortOrder(asset.getSortOrder())
                .createTime(asset.getCreateTime())
                .updateTime(asset.getUpdateTime())
                .build();
    }

    private ReviewStageVO loadStage(Long stageId) {
        WorkflowStage stage = workflowStageMapper.selectById(stageId);
        if (stage == null) {
            return null;
        }
        return ReviewStageVO.builder()
                .id(stage.getId())
                .name(stage.getName())
                .code(stage.getCode())
                .build();
    }

    private ReviewToolVO loadTool(Long toolId) {
        if (toolId == null) {
            return null;
        }
        AiTool tool = aiToolMapper.selectById(toolId);
        if (tool == null) {
            return null;
        }
        return ReviewToolVO.builder()
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
