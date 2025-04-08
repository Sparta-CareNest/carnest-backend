package com.carenest.business.reviewservice.infrastructure.repository;

import com.carenest.business.reviewservice.domain.model.Review;
import com.carenest.business.reviewservice.domain.repository.ReviewRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaReviewRepository extends ReviewRepository, JpaRepository<Review, UUID> {

}
