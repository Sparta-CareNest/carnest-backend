package com.carenest.business.reviewservice.domain.repository;

import com.carenest.business.reviewservice.domain.model.Review;

public interface ReviewRepository {
    Review save(Review review);
}
