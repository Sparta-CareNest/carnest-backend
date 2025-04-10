package com.carenest.business.reviewservice.application.service;

import com.carenest.business.reviewservice.application.dto.request.ReviewCreateRequestDto;
import com.carenest.business.reviewservice.application.dto.request.ReviewSearchRequestDto;
import com.carenest.business.reviewservice.application.dto.request.ReviewUpdateRequestDto;
import com.carenest.business.reviewservice.application.dto.response.ReviewCreateResponseDto;
import com.carenest.business.reviewservice.application.dto.response.ReviewSearchResponseDto;
import com.carenest.business.reviewservice.application.dto.response.ReviewUpdateResponseDto;
import com.carenest.business.reviewservice.domain.model.Review;
import com.carenest.business.reviewservice.domain.repository.ReviewRepository;
import com.carenest.business.reviewservice.domain.repository.ReviewRepositoryCustom;
import com.carenest.business.reviewservice.exception.ErrorCode;
import com.carenest.business.reviewservice.exception.ReviewException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewRepositoryCustom reviewRepositoryCustom;

    @Transactional
    public ReviewCreateResponseDto createReview(ReviewCreateRequestDto requestDto) {
        Review review = Review.builder()
                .reservationId(requestDto.getReservationId())
                .caregiverId(requestDto.getCaregiverId())
                .userId(UUID.randomUUID())
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .build();

        Review savedReview = reviewRepository.save(review);

        return ReviewCreateResponseDto.fromEntity(savedReview);
    }

    @Transactional(readOnly = true)
    public ReviewCreateResponseDto getReviewById(UUID reviewId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));
        return ReviewCreateResponseDto.fromEntity(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewCreateResponseDto> getAllReviews(){
        List<Review> reviews = reviewRepository.findAllByIsDeletedFalse();
        return reviews.stream()
                .map(ReviewCreateResponseDto::fromEntity)
                .collect(Collectors.toList());

    }

    @Transactional
    public ReviewUpdateResponseDto updateReview(UUID reviewId, ReviewUpdateRequestDto requestDto){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

        review.update(requestDto.getRating(), requestDto.getContent());

        return ReviewUpdateResponseDto.fromEntity(review);
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

        review.softDelete();
    }

    @Transactional(readOnly = true)
    public List<ReviewSearchResponseDto> searchReviews(ReviewSearchRequestDto searchRequestDto) {
        List<Review> reviews = reviewRepositoryCustom.searchReviews(searchRequestDto);

        return reviews.stream()
                .map(ReviewSearchResponseDto::fromEntity)
                .toList();
    }

}
