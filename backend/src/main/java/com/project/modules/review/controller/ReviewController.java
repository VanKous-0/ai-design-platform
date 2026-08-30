package com.project.modules.review.controller;

import com.project.common.result.Result;
import com.project.modules.review.service.ReviewService;
import com.project.modules.review.vo.ReviewAssetVO;
import com.project.modules.review.vo.ReviewRecordDetailVO;
import com.project.modules.review.vo.ReviewRecordListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public Result<?> listReviews(
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) Long toolId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize
    ) {
        if (pageNum != null || pageSize != null) {
            return Result.success(reviewService.pageReviews(
                    stageId,
                    toolId,
                    keyword,
                    pageNum,
                    pageSize
            ));
        }
        return Result.success(reviewService.listReviews(stageId, toolId, keyword));
    }

    @GetMapping("/{id}")
    public Result<ReviewRecordDetailVO> getReview(@PathVariable Long id) {
        return Result.success(reviewService.getReviewDetail(id));
    }

    @GetMapping("/{id}/assets")
    public Result<List<ReviewAssetVO>> listAssets(@PathVariable Long id) {
        return Result.success(reviewService.listAssets(id));
    }

    @GetMapping("/recommend")
    public Result<List<ReviewRecordListVO>> recommendReviews(
            @RequestParam Long stageId,
            @RequestParam(required = false) Long toolId
    ) {
        return Result.success(reviewService.recommendReviews(stageId, toolId));
    }
}
