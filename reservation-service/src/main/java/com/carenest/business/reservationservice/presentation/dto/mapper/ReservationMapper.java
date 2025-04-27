package com.carenest.business.reservationservice.presentation.dto.mapper;

import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.presentation.dto.response.ReservationResponse;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponse toDto(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        ReservationResponse response = new ReservationResponse();
        response.setReservationId(reservation.getReservationId());
        response.setGuardianId(reservation.getGuardianId());
        response.setGuardianName(reservation.getGuardianName());
        response.setCaregiverId(reservation.getCaregiverId());
        response.setCaregiverName(reservation.getCaregiverName());
        response.setPatientName(reservation.getPatientName());
        response.setPatientAge(reservation.getPatientAge());
        response.setPatientGender(reservation.getPatientGender());
        response.setPatientCondition(reservation.getPatientCondition());
        response.setCareAddress(reservation.getCareAddress());
        response.setStartedAt(reservation.getStartedAt());
        response.setEndedAt(reservation.getEndedAt());
        response.setServiceType(reservation.getServiceType());
        response.setServiceRequests(reservation.getServiceRequests());
        response.setTotalAmount(reservation.getTotalAmount());
        response.setServiceFee(reservation.getServiceFee());
        response.setStatus(reservation.getStatus());
        response.setAcceptedAt(reservation.getAcceptedAt());
        response.setRejectedAt(reservation.getRejectedAt());
        response.setCompletedAt(reservation.getCompletedAt());
        response.setCancelReason(reservation.getCancelReason());
        response.setRejectionReason(reservation.getRejectionReason());
        response.setCaregiverNote(reservation.getCaregiverNote());
        response.setCreatedAt(reservation.getCreatedAt());
        response.setUpdatedAt(reservation.getUpdatedAt());
        response.setPaymentId(reservation.getPaymentId());
        response.setPaymentStatus(reservation.getPaymentStatus());

        return response;
    }
}