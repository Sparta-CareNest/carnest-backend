package com.carenest.business.reviewservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reviewservice.application.dto.request.ReviewCreateRequestDto;
import com.carenest.business.reviewservice.application.dto.request.ReviewUpdateRequestDto;
import com.carenest.business.reviewservice.application.dto.response.ReviewCreateResponseDto;
import com.carenest.business.reviewservice.application.dto.response.ReviewUpdateResponseDto;
import com.carenest.business.reviewservice.application.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseDto<ReviewCreateResponseDto> createReview(@RequestBody @Valid ReviewCreateRequestDto requestDto){
        ReviewCreateResponseDto responseDto = reviewService.createReview(requestDto);
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
    public ResponseDto<ReviewUpdateResponseDto> updateReview(@PathVariable UUID reviewId,
                                                              @RequestBody @Valid ReviewUpdateRequestDto requestDto){
        ReviewUpdateResponseDto responseDto = reviewService.updateReview(reviewId, requestDto);
        return ResponseDto.success("리뷰 수정 성공", responseDto);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseDto<?> deleteReview(@PathVariable UUID reviewId){
        reviewService.deleteReview(reviewId);
        return ResponseDto.success("리뷰 삭제 성공", null);
    }

}
