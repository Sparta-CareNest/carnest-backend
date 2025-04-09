package com.carenest.business.reviewservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reviewservice.application.dto.request.ReviewCreateRequestDto;
import com.carenest.business.reviewservice.application.dto.response.ReviewResponseDto;
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
    public ResponseDto<ReviewResponseDto> createReview(@RequestBody @Valid ReviewCreateRequestDto requestDto){
        ReviewResponseDto responseDto = reviewService.createReview(requestDto);
        return ResponseDto.success("리뷰 등록 성공",responseDto);
    }

    @GetMapping("/{reviewId}")
    public ResponseDto<ReviewResponseDto> getReviewById(@PathVariable UUID reviewId){
        ReviewResponseDto responseDto = reviewService.getReviewById(reviewId);
        return ResponseDto.success("리뷰 조회 성공", responseDto);
    }

    @GetMapping
    public ResponseDto<List<ReviewResponseDto>> getAllReviews(){
        List<ReviewResponseDto> reponses = reviewService.getAllReviews();
        return ResponseDto.success("리뷰 전체 조회 성공", reponses);
    }
}
