package com.carenest.business.reservationservice.domain.repository;

import com.carenest.business.reservationservice.domain.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByGuardianId(UUID guardianId);

    List<Reservation> findByCaregiverId(UUID caregiverId);

    List<Reservation> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Reservation> findByGuardianIdAndStartedAtBetween(UUID guardianId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findByCaregiverIdAndStartedAtBetween(UUID caregiverId, LocalDateTime start, LocalDateTime end);

    Optional<Reservation> findByReservationIdAndGuardianId(UUID reservationId, UUID guardianId);

    Optional<Reservation> findByReservationIdAndCaregiverId(UUID reservationId, UUID caregiverId);
}