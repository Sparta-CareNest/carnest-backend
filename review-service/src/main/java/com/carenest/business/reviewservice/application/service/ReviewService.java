package com.carenest.business.reviewservice.application.service;

import com.carenest.business.reviewservice.application.dto.request.ReviewCreateRequestDto;
import com.carenest.business.reviewservice.application.dto.response.ReviewResponseDto;
import com.carenest.business.reviewservice.domain.model.Review;
import com.carenest.business.reviewservice.domain.repository.ReviewRepository;
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

    @Transactional
    public ReviewResponseDto createReview(ReviewCreateRequestDto requestDto) {
        Review review = Review.builder()
                .reservationId(requestDto.getReservationId())
                .caregiverId(requestDto.getCaregiverId())
                .userId(UUID.randomUUID())
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .build();

        Review savedReview = reviewRepository.save(review);

        return ReviewResponseDto.fromEntity(savedReview);
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewById(UUID reviewId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰가 없습니다."));
        return ReviewResponseDto.fromEntity(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getAllReviews(){
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .map(ReviewResponseDto::fromEntity)
                .collect(Collectors.toList());

    }




}
