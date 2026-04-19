package com.project.modules.review.service;

import com.project.modules.review.dto.ReviewAssetCreateRequest;
import com.project.modules.review.dto.ReviewAssetUpdateRequest;
import com.project.modules.review.dto.ReviewRecordCreateRequest;
import com.project.modules.review.dto.ReviewRecordUpdateRequest;
import com.project.modules.review.vo.ReviewAssetVO;
import com.project.modules.review.vo.ReviewRecordDetailVO;
import com.project.modules.review.vo.ReviewRecordListVO;

import java.util.List;

public interface ReviewService {

    List<ReviewRecordListVO> listReviews(Long stageId, Long toolId, String keyword);

    ReviewRecordDetailVO getReviewDetail(Long id);

    List<ReviewAssetVO> listAssets(Long reviewId);

    List<ReviewRecordListVO> recommendReviews(Long stageId, Long toolId);

    ReviewRecordDetailVO createReview(ReviewRecordCreateRequest request);

    ReviewRecordDetailVO updateReview(Long id, ReviewRecordUpdateRequest request);

    void deleteReview(Long id);

    ReviewAssetVO createAsset(Long reviewId, ReviewAssetCreateRequest request);

    ReviewAssetVO updateAsset(Long id, ReviewAssetUpdateRequest request);

    void deleteAsset(Long id);
}
