package com.carenest.business.reservationservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reservationservice.application.service.ReservationService;
import com.carenest.business.reservationservice.presentation.dto.response.ReservationResponse;
import com.carenest.business.reservationservice.presentation.dto.request.ReservationAcceptRequest;
import com.carenest.business.reservationservice.presentation.dto.request.ReservationRejectRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
@Tag(name = "Reservation Internal", description = "예약 서비스 내부 API")
public class ReservationInternalController {

    private final ReservationService reservationService;

    @Operation(
            summary = "예약 상세 조회 (내부용)",
            description = "내부 서비스 간 통신용 예약 상세 조회 API입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 정보 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음")
            }
    )
    @GetMapping("/reservations/{reservationId}")
    public ResponseDto<ReservationResponse> getReservationDetails(
            @Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId) {
        ReservationResponse reservation = reservationService.getReservation(reservationId);
        return ResponseDto.success("예약 정보 조회 성공", reservation);
    }

    @Operation(
            summary = "예약 수락 (내부용)",
            description = "내부 서비스 간 통신용 예약 수락 API입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 수락 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 예약 상태"),
                    @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음")
            }
    )
    @PatchMapping("/reservations/{reservationId}/accept")
    public ResponseDto<ReservationResponse> acceptReservation(
            @Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId,
            @Parameter(description = "간병인 메모 정보", required = true) @RequestBody @Valid ReservationAcceptRequest request) {

        ReservationResponse response = reservationService.acceptReservation(
                reservationId,
                request.getCaregiverNote()
        );

        return ResponseDto.success("예약이 성공적으로 수락되었습니다.", response);
    }

    @Operation(
            summary = "예약 거절 (내부용)",
            description = "내부 서비스 간 통신용 예약 거절 API입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 거절 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 예약 상태"),
                    @ApiResponse(responseCode = "404", description = "예약 정보를 찾을 수 없음")
            }
    )
    @PatchMapping("/reservations/{reservationId}/reject")
    public ResponseDto<ReservationResponse> rejectReservation(
            @Parameter(description = "예약 ID", required = true) @PathVariable UUID reservationId,
            @Parameter(description = "거절 정보", required = true) @RequestBody @Valid ReservationRejectRequest request) {

        ReservationResponse response = reservationService.rejectReservation(
                reservationId,
                request.getRejectionReason(),
                request.getSuggestedAlternative()
        );

        return ResponseDto.success("예약이 거절되었습니다.", response);
    }
}