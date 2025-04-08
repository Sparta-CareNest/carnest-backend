package com.carenest.business.paymentservice.application.service;

import com.carenest.business.paymentservice.application.dto.request.PaymentCompleteRequest;
import com.carenest.business.paymentservice.application.dto.request.PaymentCreateRequest;
import com.carenest.business.paymentservice.application.dto.request.RefundRequest;
import com.carenest.business.paymentservice.application.dto.response.PaymentResponse;
import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.domain.model.PaymentStatus;
import com.carenest.business.paymentservice.domain.service.PaymentDomainService;
import com.carenest.business.paymentservice.exception.InvalidPaymentStatusException;
import com.carenest.business.paymentservice.exception.PaymentNotFoundException;
import com.carenest.business.paymentservice.infrastructure.repository.PaymentHistoryRepository;
import com.carenest.business.paymentservice.infrastructure.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentDomainService paymentDomainService;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        // 동일한 예약에 대해 결제가 이미 존재하는지 확인
        Optional<Payment> existingPayment = paymentRepository.findByReservationId(request.getReservationId());
        if (existingPayment.isPresent()) {
            // 이미 존재하는 결제가 PENDING 상태인 경우 해당 결제 반환
            if (existingPayment.get().getStatus() == PaymentStatus.PENDING) {
                return new PaymentResponse(existingPayment.get());
            }
            // TODO: 다른 상태인 경우 예외 필요하면 추가하기
        }

        Payment payment = new Payment(
                request.getReservationId(),
                request.getGuardianId(),
                request.getCaregiverId(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getPaymentMethodDetail(),
                request.getPaymentGateway()
        );

        Payment savedPayment = paymentRepository.save(payment);
        paymentDomainService.createPaymentHistory(savedPayment);

        return new PaymentResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);

        return new PaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReservationId(UUID reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(PaymentNotFoundException::new);

        return new PaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPayments(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now().plusMonths(1);
        }

        Page<Payment> payments = paymentRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        return payments.map(PaymentResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now().plusMonths(1);
        }

        // 보호자 ID로 조회
        Page<Payment> paymentsByGuardian = paymentRepository.findByGuardianIdAndCreatedAtBetween(
                userId, startDate, endDate, pageable);

        if (!paymentsByGuardian.isEmpty()) {
            return paymentsByGuardian.map(PaymentResponse::new);
        }

        // 간병인 ID로 조회
        Page<Payment> paymentsByCaregiver = paymentRepository.findByCaregiverIdAndCreatedAtBetween(
                userId, startDate, endDate, pageable);

        return paymentsByCaregiver.map(PaymentResponse::new);
    }

    @Override
    @Transactional
    public PaymentResponse completePayment(UUID paymentId, PaymentCompleteRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStatusException();
        }

        payment.completePayment(
                request.getApprovalNumber(),
                request.getPgTransactionId(),
                request.getReceiptUrl(),
                request.getPaymentKey()
        );

        Payment savedPayment = paymentRepository.save(payment);
        paymentDomainService.createPaymentHistory(savedPayment);

        return new PaymentResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(UUID paymentId, String cancelReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);

        if (!paymentDomainService.canCancelPayment(paymentId)) {
            throw new InvalidPaymentStatusException();
        }

        payment.cancelPayment(cancelReason);
        Payment savedPayment = paymentRepository.save(payment);
        paymentDomainService.createPaymentHistory(savedPayment);

        return new PaymentResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(UUID paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);

        if (!paymentDomainService.canRefundPayment(paymentId)) {
            throw new InvalidPaymentStatusException();
        }

        payment.processRefund(
                request.getRefundAmount(),
                request.getRefundBank(),
                request.getRefundAccount(),
                request.getRefundAccountOwner()
        );

        Payment savedPayment = paymentRepository.save(payment);
        paymentDomainService.createPaymentHistory(savedPayment);

        return new PaymentResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentHistory(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now().plusMonths(1);
        }

        Page<Payment> payments = paymentRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        return payments.map(PaymentResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPaymentHistory(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now().plusMonths(1);
        }

        // 보호자 ID로 조회
        Page<Payment> paymentsByGuardian = paymentRepository.findByGuardianIdAndCreatedAtBetween(
                userId, startDate, endDate, pageable);

        if (!paymentsByGuardian.isEmpty()) {
            return paymentsByGuardian.map(PaymentResponse::new);
        }

        // 간병인 ID로 조회
        Page<Payment> paymentsByCaregiver = paymentRepository.findByCaregiverIdAndCreatedAtBetween(
                userId, startDate, endDate, pageable);

        return paymentsByCaregiver.map(PaymentResponse::new);
    }
}