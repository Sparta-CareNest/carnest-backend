package com.carenest.business.paymentservice.infrastructure.repository;

import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.domain.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByReservationId(UUID reservationId);

    Optional<Payment> findByPaymentKey(String paymentKey);

    List<Payment> findByGuardianId(UUID guardianId);

    List<Payment> findByCaregiverId(UUID caregiverId);

    List<Payment> findByStatus(PaymentStatus status);

    Page<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Payment> findByGuardianIdAndCreatedAtBetween(UUID guardianId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Payment> findByCaregiverIdAndCreatedAtBetween(UUID caregiverId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}