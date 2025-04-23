package com.carenest.business.reservationservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reservationservice.application.service.ReservationService;
import com.carenest.business.reservationservice.presentation.dto.response.ReservationResponse;
import com.carenest.business.reservationservice.presentation.dto.request.ReservationAcceptRequest;
import com.carenest.business.reservationservice.presentation.dto.request.ReservationRejectRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class ReservationInternalController {

    private final ReservationService reservationService;

    @GetMapping("/reservations/{reservationId}")
    public ResponseDto<ReservationResponse> getReservationDetails(@PathVariable UUID reservationId) {
        ReservationResponse reservation = reservationService.getReservation(reservationId);
        return ResponseDto.success("예약 정보 조회 성공", reservation);
    }

    @PostMapping("/reservations/{reservationId}/accept")
    public ResponseDto<ReservationResponse> acceptReservation(
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationAcceptRequest request) {

        ReservationResponse response = reservationService.acceptReservation(
                reservationId,
                request.getCaregiverNote()
        );

        return ResponseDto.success("예약이 성공적으로 수락되었습니다.", response);
    }

    @PostMapping("/reservations/{reservationId}/reject")
    public ResponseDto<ReservationResponse> rejectReservation(
            @PathVariable UUID reservationId,
            @RequestBody @Valid ReservationRejectRequest request) {

        ReservationResponse response = reservationService.rejectReservation(
                reservationId,
                request.getRejectionReason(),
                request.getSuggestedAlternative()
        );

        return ResponseDto.success("예약이 거절되었습니다.", response);
    }
}