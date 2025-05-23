package com.carenest.business.reviewservice.application.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carenest.business.common.exception.BaseException;
import com.carenest.business.common.exception.CommonErrorCode;
import com.carenest.business.reviewservice.application.dto.request.ReviewCreateRequestDto;
import com.carenest.business.reviewservice.application.dto.request.ReviewSearchRequestDto;
import com.carenest.business.reviewservice.application.dto.request.ReviewUpdateRequestDto;
import com.carenest.business.reviewservice.application.dto.response.CaregiverRatingDto;
import com.carenest.business.reviewservice.application.dto.response.CaregiverTopRatingDto;
import com.carenest.business.reviewservice.application.dto.response.ReviewCreateResponseDto;
import com.carenest.business.reviewservice.application.dto.response.ReviewSearchResponseDto;
import com.carenest.business.reviewservice.application.dto.response.ReviewUpdateResponseDto;
import com.carenest.business.reviewservice.domain.model.Review;
import com.carenest.business.reviewservice.domain.repository.ReviewRepository;
import com.carenest.business.reviewservice.domain.repository.ReviewRepositoryCustom;
import com.carenest.business.reviewservice.exception.ErrorCode;
import com.carenest.business.reviewservice.exception.ReviewException;
import com.carenest.business.reviewservice.infrastructure.client.CaregiverInternalClient;
import com.carenest.business.reviewservice.infrastructure.client.UserInternalClient;
import com.carenest.business.reviewservice.infrastructure.kafka.CaregiverRatingProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewRepositoryCustom reviewRepositoryCustom;
    private final CaregiverInternalClient caregiverInternalClient;
    private final CaregiverRatingProducer caregiverRatingProducer;
    private final UserInternalClient userInternalClient;


    // 리뷰생성
    @Transactional
    public ReviewCreateResponseDto createReview(UUID userId, ReviewCreateRequestDto requestDto) {

        // 사용자 존재 여부 검증
        if (!Boolean.TRUE.equals(userInternalClient.isExistedUser(userId))) {
            throw new BaseException(CommonErrorCode.INVALID_USER_STATUS);
        }

        // 간병인 존재 여부 검증
        if (!Boolean.TRUE.equals(caregiverInternalClient.isExistedCaregiver(requestDto.getCaregiverId()))) {
            throw new ReviewException(ErrorCode.INVALID_CAREGIVER);
        }

        Review review = Review.builder()
                .reservationId(requestDto.getReservationId())
                .caregiverId(requestDto.getCaregiverId())
                .userId(userId)
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .build();

        // kafka
        Review savedReview = reviewRepository.save(review);
        recalculateAndSendRating(requestDto.getCaregiverId());

        return ReviewCreateResponseDto.fromEntity(savedReview);
    }


    // 평점 재계산 후 Kafka 메시지 발행 메서드
    private void recalculateAndSendRating(UUID caregiverId) {
        List<Review> reviews = reviewRepository.findAllByCaregiverIdAndIsDeletedFalse(caregiverId);

        double average = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        caregiverRatingProducer.sendReviewUpdateEvent(caregiverId, average);
    }

    // 리뷰 단일 조회
    @Transactional(readOnly = true)
    public ReviewCreateResponseDto getReviewById(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));
        return ReviewCreateResponseDto.fromEntity(review);
    }

    // 리뷰 전체 조회
    @Transactional(readOnly = true)
    public List<ReviewCreateResponseDto> getAllReviews() {
        List<Review> reviews = reviewRepository.findAllByIsDeletedFalse();
        return reviews.stream()
                .map(ReviewCreateResponseDto::fromEntity)
                .collect(Collectors.toList());

    }

    // 리뷰 수정
    @Transactional
    public ReviewUpdateResponseDto updateReview(UUID userId, UUID reviewId, ReviewUpdateRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUserId().equals(userId)) {
            throw new BaseException(CommonErrorCode.INVALID_USER_STATUS);
        }

        review.update(requestDto.getRating(), requestDto.getContent());

        // kafka
        reviewRepository.save(review);
        recalculateAndSendRating(review.getCaregiverId());

        return ReviewUpdateResponseDto.fromEntity(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(UUID userId, UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUserId().equals(userId)) {
            throw new BaseException(CommonErrorCode.INVALID_USER_STATUS);
        }

        //kafka
        review.softDelete();
        recalculateAndSendRating(review.getCaregiverId());
    }

    // 리뷰 검색
    @Transactional(readOnly = true)
    public List<ReviewSearchResponseDto> searchReviews(ReviewSearchRequestDto searchRequestDto) {
        List<Review> reviews = reviewRepositoryCustom.searchReviews(searchRequestDto);

        return reviews.stream()
                .map(ReviewSearchResponseDto::fromEntity)
                .toList();
    }

    // 간병인 평균 평점 조회
    @Transactional(readOnly = true)
    public CaregiverRatingDto getCaregiverRating(UUID caregiverId) {
        List<Review> reviews = reviewRepository.findAllByCaregiverIdAndIsDeletedFalse(caregiverId);

        if (reviews.isEmpty()) {
            return new CaregiverRatingDto(caregiverId, 0.0, 0L); // 리뷰 없는 경우 처리
        }

        double sum = 0.0;
        for (Review review : reviews) {
            sum += review.getRating();
        }

        double average = sum / reviews.size();
        long reviewCount = reviews.size();

        return new CaregiverRatingDto(caregiverId, average, reviewCount);
    }

    // 인기간병인 조회
    @Transactional(readOnly = true)
    public List<CaregiverTopRatingDto> getTop10Caregivers() {
        List<Review> reviews = reviewRepository.findAllByIsDeletedFalse();

        return reviews.stream()
                .collect(Collectors.groupingBy(Review::getCaregiverId)) // 간병인 ID로 그룹핑
                .entrySet().stream()
                .map(entry -> {
                    UUID caregiverId = entry.getKey();
                    List<Review> caregiverReviews = entry.getValue();

                    double average = caregiverReviews.stream()
                            .mapToDouble(Review::getRating)
                            .average()
                            .orElse(0.0);

                    long reviewCount = caregiverReviews.size();

                    return new CaregiverTopRatingDto(caregiverId, average, reviewCount);
                })
                .sorted((a, b) -> {
                    int result = Double.compare(b.getAverageRating(), a.getAverageRating());
                    if (result == 0) {
                        return Long.compare(b.getReviewCount(), a.getReviewCount());
                    }
                    return result;
                })
                .limit(10)
                .toList();
    }
}