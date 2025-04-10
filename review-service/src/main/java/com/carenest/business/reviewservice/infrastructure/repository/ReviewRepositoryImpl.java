package com.carenest.business.reviewservice.infrastructure.repository;

import com.carenest.business.reviewservice.application.dto.request.ReviewSearchRequestDto;
import com.carenest.business.reviewservice.domain.model.QReview;
import com.carenest.business.reviewservice.domain.model.Review;
import com.carenest.business.reviewservice.domain.repository.ReviewRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Review> searchReviews(ReviewSearchRequestDto condition) {
        QReview review = QReview.review;

        return jpaQueryFactory
                .selectFrom(review)
                .where(
                        caregiverIdEq(condition.getCaregiverId()),
                        ratingEq(condition.getRating()),
                        review.isDeleted.isFalse()
                )
                .fetch();
    }

    private BooleanExpression caregiverIdEq(UUID caregiverId) {
        return caregiverId != null ? QReview.review.caregiverId.eq(caregiverId) : null;
    }

    private BooleanExpression ratingEq(Integer rating) {
        return rating != null ? QReview.review.rating.eq(rating.doubleValue()) : null;
    }
}
