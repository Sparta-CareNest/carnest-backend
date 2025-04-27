package com.carenest.business.reviewservice.domain.repository;

import com.carenest.business.reviewservice.application.dto.request.ReviewSearchRequestDto;
import com.carenest.business.reviewservice.domain.model.Review;

import java.util.List;

public interface ReviewRepositoryCustom {
    List<Review> searchReviews(ReviewSearchRequestDto searchRequestDto);
}
