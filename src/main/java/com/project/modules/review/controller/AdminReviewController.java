package com.project.modules.review.controller;

import com.project.common.result.Result;
import com.project.modules.review.dto.ReviewAssetCreateRequest;
import com.project.modules.review.dto.ReviewAssetUpdateRequest;
import com.project.modules.review.dto.ReviewRecordCreateRequest;
import com.project.modules.review.dto.ReviewRecordUpdateRequest;
import com.project.modules.review.service.ReviewService;
import com.project.modules.review.vo.ReviewAssetVO;
import com.project.modules.review.vo.ReviewRecordDetailVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    public Result<ReviewRecordDetailVO> createReview(@Valid @RequestBody ReviewRecordCreateRequest request) {
        return Result.success(reviewService.createReview(request));
    }

    @PutMapping("/reviews/{id}")
    public Result<ReviewRecordDetailVO> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRecordUpdateRequest request
    ) {
        return Result.success(reviewService.updateReview(id, request));
    }

    @DeleteMapping("/reviews/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return Result.success();
    }

    @PostMapping("/reviews/{id}/assets")
    public Result<ReviewAssetVO> createAsset(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAssetCreateRequest request
    ) {
        return Result.success(reviewService.createAsset(id, request));
    }

    @PutMapping("/review-assets/{id}")
    public Result<ReviewAssetVO> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAssetUpdateRequest request
    ) {
        return Result.success(reviewService.updateAsset(id, request));
    }

    @DeleteMapping("/review-assets/{id}")
    public Result<Void> deleteAsset(@PathVariable Long id) {
        reviewService.deleteAsset(id);
        return Result.success();
    }
}
