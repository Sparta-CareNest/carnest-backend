package com.carenest.business.reservationservice.domain.repository;

import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ReservationRepositoryCustom {

    Page<Reservation> findBySearchCriteria(
            UUID guardianId,
            UUID caregiverId,
            String patientName,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ReservationStatus status,
            Pageable pageable
    );
}