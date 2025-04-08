package com.carenest.business.reviewservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reviewservice.application.dto.request.ReviewCreateRequestDto;
import com.carenest.business.reviewservice.application.dto.response.ReviewResponseDto;
import com.carenest.business.reviewservice.application.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
