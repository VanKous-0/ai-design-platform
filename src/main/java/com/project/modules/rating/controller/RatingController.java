package com.project.modules.rating.controller;

import com.project.common.result.Result;
import com.project.modules.rating.dto.UserToolRatingSaveRequest;
import com.project.modules.rating.dto.UserWorkflowRatingSaveRequest;
import com.project.modules.rating.service.RatingService;
import com.project.modules.rating.vo.RatingSummaryVO;
import com.project.modules.rating.vo.UserToolRatingVO;
import com.project.modules.rating.vo.UserWorkflowRatingVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/tools")
    public Result<UserToolRatingVO> saveToolRating(
            Authentication authentication,
            @Valid @RequestBody UserToolRatingSaveRequest request
    ) {
        return Result.success(ratingService.saveToolRating(currentUserId(authentication), request));
    }

    @GetMapping("/tools/my")
    public Result<UserToolRatingVO> getMyToolRating(Authentication authentication, @RequestParam Long toolId) {
        return Result.success(ratingService.getMyToolRating(currentUserId(authentication), toolId));
    }

    @GetMapping("/tools/summary")
    public Result<RatingSummaryVO> getToolRatingSummary(@RequestParam Long toolId) {
        return Result.success(ratingService.getToolRatingSummary(toolId));
    }

    @PostMapping("/workflows")
    public Result<UserWorkflowRatingVO> saveWorkflowRating(
            Authentication authentication,
            @Valid @RequestBody UserWorkflowRatingSaveRequest request
    ) {
        return Result.success(ratingService.saveWorkflowRating(currentUserId(authentication), request));
    }

    @GetMapping("/workflows/my")
    public Result<UserWorkflowRatingVO> getMyWorkflowRating(Authentication authentication, @RequestParam Long instanceId) {
        return Result.success(ratingService.getMyWorkflowRating(currentUserId(authentication), instanceId));
    }

    @GetMapping("/workflows/summary")
    public Result<RatingSummaryVO> getWorkflowRatingSummary(
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) Long instanceId
    ) {
        return Result.success(ratingService.getWorkflowRatingSummary(templateId, instanceId));
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
