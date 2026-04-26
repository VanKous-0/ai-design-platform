package com.project.modules.rating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.common.exception.BusinessException;
import com.project.modules.rating.dto.UserToolRatingSaveRequest;
import com.project.modules.rating.dto.UserWorkflowRatingSaveRequest;
import com.project.modules.rating.entity.UserToolRating;
import com.project.modules.rating.entity.UserWorkflowRating;
import com.project.modules.rating.mapper.UserToolRatingMapper;
import com.project.modules.rating.mapper.UserWorkflowRatingMapper;
import com.project.modules.rating.service.RatingService;
import com.project.modules.rating.vo.RatingSummaryVO;
import com.project.modules.rating.vo.UserToolRatingVO;
import com.project.modules.rating.vo.UserWorkflowRatingVO;
import com.project.modules.tool.entity.AiTool;
import com.project.modules.tool.mapper.AiToolMapper;
import com.project.modules.workflow.runtime.entity.WorkflowInstance;
import com.project.modules.workflow.runtime.entity.WorkflowTemplate;
import com.project.modules.workflow.runtime.mapper.WorkflowInstanceMapper;
import com.project.modules.workflow.runtime.mapper.WorkflowTemplateMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class RatingServiceImpl implements RatingService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);

    private final UserToolRatingMapper toolRatingMapper;
    private final UserWorkflowRatingMapper workflowRatingMapper;
    private final AiToolMapper aiToolMapper;
    private final WorkflowTemplateMapper workflowTemplateMapper;
    private final WorkflowInstanceMapper workflowInstanceMapper;

    public RatingServiceImpl(
            UserToolRatingMapper toolRatingMapper,
            UserWorkflowRatingMapper workflowRatingMapper,
            AiToolMapper aiToolMapper,
            WorkflowTemplateMapper workflowTemplateMapper,
            WorkflowInstanceMapper workflowInstanceMapper
    ) {
        this.toolRatingMapper = toolRatingMapper;
        this.workflowRatingMapper = workflowRatingMapper;
        this.aiToolMapper = aiToolMapper;
        this.workflowTemplateMapper = workflowTemplateMapper;
        this.workflowInstanceMapper = workflowInstanceMapper;
    }

    @Override
    public UserToolRatingVO saveToolRating(Long userId, UserToolRatingSaveRequest request) {
        ensureToolExists(request.getToolId());
        UserToolRating rating = toolRatingMapper.selectOne(new LambdaQueryWrapper<UserToolRating>()
                .eq(UserToolRating::getUserId, userId)
                .eq(UserToolRating::getToolId, request.getToolId())
                .last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (rating == null) {
            rating = new UserToolRating();
            rating.setUserId(userId);
            rating.setToolId(request.getToolId());
            rating.setCreateTime(now);
            rating.setIsDeleted(0);
        }
        fillToolRating(rating, request);
        rating.setUpdateTime(now);
        if (rating.getId() == null) {
            toolRatingMapper.insert(rating);
        } else {
            toolRatingMapper.updateById(rating);
        }
        return toToolRatingVO(rating);
    }

    @Override
    public UserToolRatingVO getMyToolRating(Long userId, Long toolId) {
        ensureToolExists(toolId);
        UserToolRating rating = toolRatingMapper.selectOne(new LambdaQueryWrapper<UserToolRating>()
                .eq(UserToolRating::getUserId, userId)
                .eq(UserToolRating::getToolId, toolId)
                .last("limit 1"));
        return rating == null ? null : toToolRatingVO(rating);
    }

    @Override
    public RatingSummaryVO getToolRatingSummary(Long toolId) {
        ensureToolExists(toolId);
        List<UserToolRating> ratings = toolRatingMapper.selectList(new LambdaQueryWrapper<UserToolRating>()
                .eq(UserToolRating::getToolId, toolId));
        return summarize("tool", toolId, ratings.stream()
                .map(this::scores)
                .toList());
    }

    @Override
    public UserWorkflowRatingVO saveWorkflowRating(Long userId, UserWorkflowRatingSaveRequest request) {
        ensureWorkflowTemplateExists(request.getTemplateId());
        WorkflowInstance instance = ensureOwnedWorkflowInstance(userId, request.getInstanceId());
        if (!Objects.equals(instance.getTemplateId(), request.getTemplateId())) {
            throw new BusinessException("Template ID does not match workflow instance");
        }

        UserWorkflowRating rating = workflowRatingMapper.selectOne(new LambdaQueryWrapper<UserWorkflowRating>()
                .eq(UserWorkflowRating::getUserId, userId)
                .eq(UserWorkflowRating::getTemplateId, request.getTemplateId())
                .eq(UserWorkflowRating::getInstanceId, request.getInstanceId())
                .last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (rating == null) {
            rating = new UserWorkflowRating();
            rating.setUserId(userId);
            rating.setTemplateId(request.getTemplateId());
            rating.setInstanceId(request.getInstanceId());
            rating.setCreateTime(now);
            rating.setIsDeleted(0);
        }
        fillWorkflowRating(rating, request);
        rating.setUpdateTime(now);
        if (rating.getId() == null) {
            workflowRatingMapper.insert(rating);
        } else {
            workflowRatingMapper.updateById(rating);
        }
        return toWorkflowRatingVO(rating);
    }

    @Override
    public UserWorkflowRatingVO getMyWorkflowRating(Long userId, Long instanceId) {
        ensureOwnedWorkflowInstance(userId, instanceId);
        UserWorkflowRating rating = workflowRatingMapper.selectOne(new LambdaQueryWrapper<UserWorkflowRating>()
                .eq(UserWorkflowRating::getUserId, userId)
                .eq(UserWorkflowRating::getInstanceId, instanceId)
                .last("limit 1"));
        return rating == null ? null : toWorkflowRatingVO(rating);
    }

    @Override
    public RatingSummaryVO getWorkflowRatingSummary(Long templateId, Long instanceId) {
        if (templateId == null && instanceId == null) {
            throw new BusinessException("templateId or instanceId is required");
        }
        LambdaQueryWrapper<UserWorkflowRating> query = new LambdaQueryWrapper<>();
        Long targetId;
        String targetType;
        if (instanceId != null) {
            ensureWorkflowInstanceExists(instanceId);
            query.eq(UserWorkflowRating::getInstanceId, instanceId);
            targetId = instanceId;
            targetType = "workflow_instance";
        } else {
            ensureWorkflowTemplateExists(templateId);
            query.eq(UserWorkflowRating::getTemplateId, templateId);
            targetId = templateId;
            targetType = "workflow_template";
        }
        List<UserWorkflowRating> ratings = workflowRatingMapper.selectList(query);
        return summarize(targetType, targetId, ratings.stream()
                .map(this::scores)
                .toList());
    }

    private void ensureToolExists(Long toolId) {
        AiTool tool = aiToolMapper.selectById(toolId);
        if (tool == null) {
            throw new BusinessException("AI tool does not exist");
        }
    }

    private void ensureWorkflowTemplateExists(Long templateId) {
        WorkflowTemplate template = workflowTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException("Workflow template does not exist");
        }
    }

    private WorkflowInstance ensureWorkflowInstanceExists(Long instanceId) {
        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException("Workflow instance does not exist");
        }
        return instance;
    }

    private WorkflowInstance ensureOwnedWorkflowInstance(Long userId, Long instanceId) {
        WorkflowInstance instance = workflowInstanceMapper.selectOne(new LambdaQueryWrapper<WorkflowInstance>()
                .eq(WorkflowInstance::getId, instanceId)
                .eq(WorkflowInstance::getUserId, userId)
                .last("limit 1"));
        if (instance == null) {
            throw new BusinessException("Workflow instance does not exist or you have no permission");
        }
        return instance;
    }

    private void fillToolRating(UserToolRating rating, UserToolRatingSaveRequest request) {
        rating.setEffectScore(request.getEffectScore());
        rating.setEaseScore(request.getEaseScore());
        rating.setStabilityScore(request.getStabilityScore());
        rating.setRecommendScore(request.getRecommendScore());
        rating.setComment(request.getComment());
    }

    private void fillWorkflowRating(UserWorkflowRating rating, UserWorkflowRatingSaveRequest request) {
        rating.setEffectScore(request.getEffectScore());
        rating.setEaseScore(request.getEaseScore());
        rating.setStabilityScore(request.getStabilityScore());
        rating.setRecommendScore(request.getRecommendScore());
        rating.setComment(request.getComment());
    }

    private RatingSummaryVO summarize(String targetType, Long targetId, List<BigDecimal[]> scores) {
        if (scores.isEmpty()) {
            return RatingSummaryVO.builder()
                    .targetType(targetType)
                    .targetId(targetId)
                    .ratingCount(0L)
                    .averageEffectScore(ZERO)
                    .averageEaseScore(ZERO)
                    .averageStabilityScore(ZERO)
                    .averageRecommendScore(ZERO)
                    .averageTotalScore(ZERO)
                    .build();
        }
        BigDecimal count = BigDecimal.valueOf(scores.size());
        BigDecimal effect = average(scores.stream().map(values -> values[0]).toList(), count);
        BigDecimal ease = average(scores.stream().map(values -> values[1]).toList(), count);
        BigDecimal stability = average(scores.stream().map(values -> values[2]).toList(), count);
        BigDecimal recommend = average(scores.stream().map(values -> values[3]).toList(), count);
        return RatingSummaryVO.builder()
                .targetType(targetType)
                .targetId(targetId)
                .ratingCount((long) scores.size())
                .averageEffectScore(effect)
                .averageEaseScore(ease)
                .averageStabilityScore(stability)
                .averageRecommendScore(recommend)
                .averageTotalScore(effect.add(ease).add(stability).add(recommend)
                        .divide(BigDecimal.valueOf(4), 1, RoundingMode.HALF_UP))
                .build();
    }

    private BigDecimal average(List<BigDecimal> values, BigDecimal count) {
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(count, 1, RoundingMode.HALF_UP);
    }

    private BigDecimal[] scores(UserToolRating rating) {
        return new BigDecimal[]{
                rating.getEffectScore(),
                rating.getEaseScore(),
                rating.getStabilityScore(),
                rating.getRecommendScore()
        };
    }

    private BigDecimal[] scores(UserWorkflowRating rating) {
        return new BigDecimal[]{
                rating.getEffectScore(),
                rating.getEaseScore(),
                rating.getStabilityScore(),
                rating.getRecommendScore()
        };
    }

    private BigDecimal totalScore(BigDecimal effect, BigDecimal ease, BigDecimal stability, BigDecimal recommend) {
        return effect.add(ease).add(stability).add(recommend)
                .divide(BigDecimal.valueOf(4), 1, RoundingMode.HALF_UP);
    }

    private UserToolRatingVO toToolRatingVO(UserToolRating rating) {
        return UserToolRatingVO.builder()
                .id(rating.getId())
                .userId(rating.getUserId())
                .toolId(rating.getToolId())
                .effectScore(rating.getEffectScore())
                .easeScore(rating.getEaseScore())
                .stabilityScore(rating.getStabilityScore())
                .recommendScore(rating.getRecommendScore())
                .totalScore(totalScore(rating.getEffectScore(), rating.getEaseScore(), rating.getStabilityScore(), rating.getRecommendScore()))
                .comment(rating.getComment())
                .createTime(rating.getCreateTime())
                .updateTime(rating.getUpdateTime())
                .build();
    }

    private UserWorkflowRatingVO toWorkflowRatingVO(UserWorkflowRating rating) {
        return UserWorkflowRatingVO.builder()
                .id(rating.getId())
                .userId(rating.getUserId())
                .templateId(rating.getTemplateId())
                .instanceId(rating.getInstanceId())
                .effectScore(rating.getEffectScore())
                .easeScore(rating.getEaseScore())
                .stabilityScore(rating.getStabilityScore())
                .recommendScore(rating.getRecommendScore())
                .totalScore(totalScore(rating.getEffectScore(), rating.getEaseScore(), rating.getStabilityScore(), rating.getRecommendScore()))
                .comment(rating.getComment())
                .createTime(rating.getCreateTime())
                .updateTime(rating.getUpdateTime())
                .build();
    }
}
