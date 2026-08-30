package com.project.modules.rating.service;

import com.project.modules.rating.dto.UserToolRatingSaveRequest;
import com.project.modules.rating.dto.UserWorkflowRatingSaveRequest;
import com.project.modules.rating.vo.RatingSummaryVO;
import com.project.modules.rating.vo.UserToolRatingVO;
import com.project.modules.rating.vo.UserWorkflowRatingVO;

public interface RatingService {

    UserToolRatingVO saveToolRating(Long userId, UserToolRatingSaveRequest request);

    UserToolRatingVO getMyToolRating(Long userId, Long toolId);

    RatingSummaryVO getToolRatingSummary(Long toolId);

    UserWorkflowRatingVO saveWorkflowRating(Long userId, UserWorkflowRatingSaveRequest request);

    UserWorkflowRatingVO getMyWorkflowRating(Long userId, Long instanceId);

    RatingSummaryVO getWorkflowRatingSummary(Long templateId, Long instanceId);
}
