package com.carenest.business.reviewservice.presentation.controller;

import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reviewservice.application.dto.request.ReviewCreateRequestDto;
import com.carenest.business.reviewservice.application.dto.request.ReviewSearchRequestDto;
import com.carenest.business.reviewservice.application.dto.request.ReviewUpdateRequestDto;
import com.carenest.business.reviewservice.application.dto.response.*;
import com.carenest.business.reviewservice.application.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseDto<ReviewCreateResponseDto> createReview(
            @AuthUser AuthUserInfo authUserInfo,
            @RequestBody @Valid ReviewCreateRequestDto requestDto){
        ReviewCreateResponseDto responseDto = reviewService.createReview(authUserInfo.getUserId(), requestDto);
        return ResponseDto.success("리뷰 등록 성공",responseDto);
    }

    @GetMapping("/{reviewId}")
    public ResponseDto<ReviewCreateResponseDto> getReviewById(@PathVariable UUID reviewId){
        ReviewCreateResponseDto responseDto = reviewService.getReviewById(reviewId);
        return ResponseDto.success("리뷰 조회 성공", responseDto);
    }

    @GetMapping
    public ResponseDto<List<ReviewCreateResponseDto>> getAllReviews(){
        List<ReviewCreateResponseDto> reponses = reviewService.getAllReviews();
        return ResponseDto.success("리뷰 전체 조회 성공", reponses);
    }

    @PatchMapping("/{reviewId}")
    public ResponseDto<ReviewUpdateResponseDto> updateReview(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID reviewId,
            @RequestBody @Valid ReviewUpdateRequestDto requestDto){
        ReviewUpdateResponseDto responseDto = reviewService.updateReview(authUserInfo.getUserId(),reviewId, requestDto);
        return ResponseDto.success("리뷰 수정 성공", responseDto);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseDto<?> deleteReview(
            @AuthUser AuthUserInfo authUserInfo,
            @PathVariable UUID reviewId){
        reviewService.deleteReview(authUserInfo.getUserId(),reviewId);
        return ResponseDto.success("리뷰 삭제 성공", null);
    }

    @GetMapping("/search")
    public ResponseDto<List<ReviewSearchResponseDto>> searchReviews(@ModelAttribute ReviewSearchRequestDto requestDto) {
        List<ReviewSearchResponseDto> results = reviewService.searchReviews(requestDto);
        return ResponseDto.success("리뷰 검색 성공", results);
    }

    // 평균 평점 조회 API
    @GetMapping("/ratings/{caregiverId}")
    public ResponseEntity<CaregiverRatingDto> getCaregiverRating(@PathVariable UUID caregiverId) {
        CaregiverRatingDto rating = reviewService.getCaregiverRating(caregiverId);
        return ResponseEntity.ok(rating);
    }

    // 인기 간병인 조회
    @GetMapping("/ratings/top")
    public ResponseEntity<List<CaregiverTopRatingDto>> getTop10Caregivers() {
        return ResponseEntity.ok(reviewService.getTop10Caregivers());
    }

    // 간병인 Id를 파라미터로 받아서 리뷰 조회 후 평점 반환
    @GetMapping("/ratings/calculate")
    public ResponseDto<CaregiverRatingDto> calculateRating(@RequestParam UUID caregiverId) {
        CaregiverRatingDto rating = reviewService.calculateAverageRating(caregiverId);
        return ResponseDto.success("실시간 평균 평점 조회 성공", rating);
    }


}
