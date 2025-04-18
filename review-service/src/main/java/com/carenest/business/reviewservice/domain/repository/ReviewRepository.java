package com.carenest.business.reviewservice.domain.repository;

import com.carenest.business.reviewservice.domain.model.Review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {
    Review save(Review review);
    Optional<Review> findById(UUID reviewId);
    List<Review> findAllByIsDeletedFalse();

    //삭제되지 않은 간병인의 리뷰만 조회
    List<Review> findAllByCaregiverIdAndIsDeletedFalse(UUID caregiverId);
}
