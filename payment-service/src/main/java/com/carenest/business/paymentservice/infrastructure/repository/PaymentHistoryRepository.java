package com.carenest.business.paymentservice.infrastructure.repository;

import com.carenest.business.paymentservice.domain.model.PaymentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, UUID> {
    List<PaymentHistory> findByPaymentId(UUID paymentId);

    List<PaymentHistory> findByReservationId(UUID reservationId);

    Page<PaymentHistory> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<PaymentHistory> findByGuardianIdAndCreatedAtBetween(UUID guardianId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<PaymentHistory> findByCaregiverIdAndCreatedAtBetween(UUID caregiverId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}