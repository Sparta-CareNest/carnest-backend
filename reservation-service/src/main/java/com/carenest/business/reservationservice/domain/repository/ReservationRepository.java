package com.carenest.business.reservationservice.domain.repository;

import com.carenest.business.reservationservice.domain.model.Reservation;
import com.carenest.business.reservationservice.domain.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID>, ReservationRepositoryCustom {
    List<Reservation> findByGuardianId(UUID guardianId);

    List<Reservation> findByCaregiverId(UUID caregiverId);

    List<Reservation> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Reservation> findByGuardianIdAndStartedAtBetween(UUID guardianId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findByCaregiverIdAndStartedAtBetween(UUID caregiverId, LocalDateTime start, LocalDateTime end);

    Optional<Reservation> findByReservationIdAndGuardianId(UUID reservationId, UUID guardianId);

    Optional<Reservation> findByReservationIdAndCaregiverId(UUID reservationId, UUID caregiverId);

    Page<Reservation> findByStartedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Reservation> findByGuardianIdAndStartedAtBetween(UUID guardianId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Reservation> findByCaregiverIdAndStartedAtBetween(UUID caregiverId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);
}