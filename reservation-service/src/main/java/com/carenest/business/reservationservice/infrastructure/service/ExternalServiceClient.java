package com.carenest.business.reservationservice.infrastructure.service;

import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.infrastructure.client.dto.response.CaregiverDetailResponseDto;

import java.util.UUID;

public interface ExternalServiceClient {

    UUID requestPayment(Reservation reservation);

    boolean cancelPayment(UUID paymentId, String cancelReason);

    void sendReservationCreatedNotification(UUID userId, String message);

    void sendPaymentCompletedNotification(UUID userId, String message);

    CaregiverDetailResponseDto getCaregiverDetail(UUID caregiverId);
}