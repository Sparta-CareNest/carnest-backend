package com.carenest.business.reviewservice.domain.repository;

import com.carenest.business.reviewservice.domain.model.Review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {
    Review save(Review review);
    Optional<Review> findById(UUID orderId);
    List<Review> findAll();
}
