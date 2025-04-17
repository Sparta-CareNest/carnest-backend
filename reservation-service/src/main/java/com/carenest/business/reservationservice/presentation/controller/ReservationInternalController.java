package com.carenest.business.reservationservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.reservationservice.application.service.ReservationService;
import com.carenest.business.reservationservice.presentation.dto.response.ReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}