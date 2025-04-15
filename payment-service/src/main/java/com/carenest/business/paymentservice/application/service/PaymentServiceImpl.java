package com.carenest.business.paymentservice.application.service;

import com.carenest.business.paymentservice.application.dto.request.PaymentCompleteRequest;
import com.carenest.business.paymentservice.application.dto.request.PaymentCreateRequest;
import com.carenest.business.paymentservice.application.dto.request.RefundRequest;
import com.carenest.business.paymentservice.application.dto.response.PaymentHistoryDetailResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentHistoryResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentListResponse;
import com.carenest.business.paymentservice.application.dto.response.PaymentResponse;
import com.carenest.business.paymentservice.domain.model.Payment;
import com.carenest.business.paymentservice.domain.model.PaymentHistory;
import com.carenest.business.paymentservice.domain.model.PaymentStatus;
import com.carenest.business.paymentservice.domain.service.PaymentDomainService;
import com.carenest.business.paymentservice.exception.*;
import com.carenest.business.paymentservice.infrastructure.external.PaymentGatewayService;
import com.carenest.business.paymentservice.infrastructure.kafka.PaymentEventProducer;
import com.carenest.business.paymentservice.infrastructure.repository.PaymentHistoryRepository;
import com.carenest.business.paymentservice.infrastructure.repository.PaymentRepository;
import com.carenest.business.paymentservice.infrastructure.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentDomainService paymentDomainService;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;
    private final PaymentEventProducer paymentEventProducer; // Kafka 이벤트 프로듀서 추가

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request, UUID guardianId) {
        log.info("결제 생성 요청: reservationId={}, amount={}", request.getReservationId(), request.getAmount());

        // 동일한 예약에 대해 결제가 이미 존재하는지 확인
        Optional<Payment> existingPayment = paymentRepository.findByReservationId(request.getReservationId());
        if (existingPayment.isPresent()) {
            if (existingPayment.get().getStatus() == PaymentStatus.PENDING) {
                log.info("이미 진행 중인 결제가 있습니다: paymentId={}", existingPayment.get().getPaymentId());
                return new PaymentResponse(existingPayment.get());
            }
            log.warn("이미 처리된 결제가 있습니다: paymentId={}, status={}",
                    existingPayment.get().getPaymentId(), existingPayment.get().getStatus());
            throw new DuplicatePaymentException();
        }

        try {
            // 결제 게이트웨이에 결제 준비 요청
            Map<String, Object> paymentPrepareResult = paymentGatewayService.preparePayment(
                    request.getReservationId(),
                    request.getAmount(),
                    request.getPaymentMethod()
            );

            Payment payment = new Payment(
                    request.getReservationId(),
                    guardianId,
                    request.getCaregiverId(),
                    request.getAmount(),
                    request.getPaymentMethod(),
                    request.getPaymentMethodDetail(),
                    request.getPaymentGateway()
            );

            // TODO: 결제 키 있으면 설정하기
            if (paymentPrepareResult.containsKey("paymentKey")) {
                payment.setPaymentKey((String) paymentPrepareResult.get("paymentKey"));
            }

            Payment savedPayment = paymentRepository.save(payment);
            paymentDomainService.createPaymentHistory(savedPayment);

            log.info("결제 생성 완료: paymentId={}", savedPayment.getPaymentId());
            return new PaymentResponse(savedPayment);
        } catch (Exception e) {
            log.error("결제 생성 중 오류 발생", e);
            if (e instanceof PaymentException) {
                throw e;
            }
            throw new PaymentException(PaymentErrorCode.PAYMENT_PROCESSING_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        log.info("결제 조회: paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("결제 정보를 찾을 수 없습니다: paymentId={}", paymentId);
                    return new PaymentNotFoundException();
                });

        return new PaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReservationId(UUID reservationId) {
        log.info("예약 ID로 결제 조회: reservationId={}", reservationId);

        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> {
                    log.warn("예약에 대한 결제 정보를 찾을 수 없습니다: reservationId={}", reservationId);
                    return new PaymentNotFoundException();
                });

        return new PaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentListResponse> getPaymentList(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        startDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
        endDate = endDate != null ? endDate : LocalDateTime.now().plusMonths(1);

        log.info("결제 목록 조회: startDate={}, endDate={}, page={}, size={}",
                startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());

        Page<Payment> payments = paymentRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        return payments.map(PaymentListResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentListResponse> getUserPaymentList(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        startDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
        endDate = endDate != null ? endDate : LocalDateTime.now().plusMonths(1);

        log.info("사용자 결제 목록 조회: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        // 보호자 ID로 조회
        Page<Payment> paymentsByGuardian = paymentRepository.findByGuardianIdAndCreatedAtBetween(
                userId, startDate, endDate, pageable);

        if (!paymentsByGuardian.isEmpty()) {
            log.info("보호자로 결제 내역 {} 건 조회됨", paymentsByGuardian.getTotalElements());
            return paymentsByGuardian.map(PaymentListResponse::new);
        }

        // 간병인 ID로 조회
        Page<Payment> paymentsByCaregiver = paymentRepository.findByCaregiverIdAndCreatedAtBetween(
                userId, startDate, endDate, pageable);

        log.info("간병인으로 결제 내역 {} 건 조회됨", paymentsByCaregiver.getTotalElements());
        return paymentsByCaregiver.map(PaymentListResponse::new);
    }

    @Override
    @Transactional
    public PaymentResponse completePayment(UUID paymentId, PaymentCompleteRequest request) {
        log.info("결제 완료 처리: paymentId={}, approvalNumber={}", paymentId, request.getApprovalNumber());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("결제 정보를 찾을 수 없습니다: paymentId={}", paymentId);
                    return new PaymentNotFoundException();
                });

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("유효하지 않은 결제 상태: paymentId={}, status={}", paymentId, payment.getStatus());
            throw new InvalidPaymentStatusException();
        }

        try {
            // 실제 게이트웨이에 승인 요청하고 결과 받기
            PaymentCompleteRequest actualResult = paymentGatewayService.approvePayment(
                    payment.getPaymentKey(), payment.getAmount());

            payment.completePayment(
                    actualResult.getApprovalNumber(),
                    actualResult.getPgTransactionId(),
                    actualResult.getReceiptUrl(),
                    actualResult.getPaymentKey()
            );

            Payment savedPayment = paymentRepository.save(payment);
            paymentDomainService.createPaymentHistory(savedPayment);

            // 결제 완료 알림 발송
            notificationService.sendPaymentSuccessNotification(savedPayment);

            // Kafka 이벤트 발행
            paymentEventProducer.sendPaymentCompletedEvent(savedPayment);

            log.info("결제 완료 처리 성공: paymentId={}", paymentId);
            return new PaymentResponse(savedPayment);
        } catch (Exception e) {
            log.error("결제 완료 처리 중 오류 발생", e);
            if (e instanceof PaymentException) {
                throw e;
            }
            throw new PaymentException(PaymentErrorCode.PAYMENT_PROCESSING_ERROR);
        }
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(UUID paymentId, String cancelReason) {
        log.info("결제 취소 요청: paymentId={}, reason={}", paymentId, cancelReason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("결제 정보를 찾을 수 없습니다: paymentId={}", paymentId);
                    return new PaymentNotFoundException();
                });

        if (!paymentDomainService.canCancelPayment(paymentId)) {
            log.warn("취소할 수 없는 상태의 결제입니다: paymentId={}, status={}", paymentId, payment.getStatus());
            throw new InvalidPaymentStatusException();
        }

        try {
            // TODO: 게이트웨이 연동 시 실제 취소 요청
            if (payment.getStatus() == PaymentStatus.COMPLETED && payment.getPaymentKey() != null) {
                boolean cancelResult = paymentGatewayService.cancelPayment(
                        payment.getPaymentKey(), payment.getAmount(), cancelReason);

                if (!cancelResult) {
                    log.error("결제 게이트웨이 취소 실패: paymentId={}", paymentId);
                    throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
                }
            }

            payment.cancelPayment(cancelReason);
            Payment savedPayment = paymentRepository.save(payment);
            paymentDomainService.createPaymentHistory(savedPayment);

            // 결제 취소 알림 발송
            notificationService.sendPaymentCancelNotification(savedPayment);

            // Kafka 이벤트 발행
            paymentEventProducer.sendPaymentCancelledEvent(savedPayment);

            log.info("결제 취소 완료: paymentId={}", paymentId);
            return new PaymentResponse(savedPayment);
        } catch (Exception e) {
            log.error("결제 취소 중 오류 발생", e);
            if (e instanceof PaymentException) {
                throw e;
            }
            throw new PaymentException(PaymentErrorCode.PAYMENT_PROCESSING_ERROR);
        }
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(UUID paymentId, RefundRequest request) {
        log.info("결제 환불 요청: paymentId={}, amount={}", paymentId, request.getRefundAmount());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("결제 정보를 찾을 수 없습니다: paymentId={}", paymentId);
                    return new PaymentNotFoundException();
                });

        if (!paymentDomainService.canRefundPayment(paymentId)) {
            log.warn("환불할 수 없는 상태의 결제입니다: paymentId={}, status={}", paymentId, payment.getStatus());
            throw new InvalidPaymentStatusException();
        }

        try {
            if (payment.getPaymentKey() != null) {
                boolean refundResult = paymentGatewayService.refundPayment(
                        payment.getPaymentKey(), request);

                if (!refundResult) {
                    log.error("결제 게이트웨이 환불 실패: paymentId={}", paymentId);
                    throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR);
                }
            }

            payment.processRefund(
                    request.getRefundAmount(),
                    request.getRefundBank(),
                    request.getRefundAccount(),
                    request.getRefundAccountOwner()
            );

            Payment savedPayment = paymentRepository.save(payment);
            paymentDomainService.createPaymentHistory(savedPayment);

            // 결제 환불 알림 발송
            notificationService.sendPaymentRefundNotification(savedPayment);

            // Kafka 이벤트 발행 (취소와 동일하게 처리)
            paymentEventProducer.sendPaymentCancelledEvent(savedPayment);

            log.info("결제 환불 완료: paymentId={}", paymentId);
            return new PaymentResponse(savedPayment);
        } catch (Exception e) {
            log.error("결제 환불 중 오류 발생", e);
            if (e instanceof PaymentException) {
                throw e;
            }
            throw new PaymentException(PaymentErrorCode.PAYMENT_PROCESSING_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentHistoryResponse> getPaymentHistoryById(UUID paymentId, Pageable pageable) {
        log.info("결제 이력 조회: paymentId={}", paymentId);

        if (!paymentRepository.existsById(paymentId)) {
            log.warn("결제 정보를 찾을 수 없습니다: paymentId={}", paymentId);
            throw new PaymentNotFoundException();
        }

        List<PaymentHistory> histories = paymentHistoryRepository.findByPaymentId(paymentId);

        if (histories.isEmpty()) {
            log.info("결제 이력이 없습니다: paymentId={}", paymentId);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), histories.size());

        if (start >= histories.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, histories.size());
        }

        List<PaymentHistoryResponse> responseList = histories.subList(start, end)
                .stream()
                .map(PaymentHistoryResponse::new)
                .collect(Collectors.toList());

        log.info("결제 이력 {} 건 조회됨: paymentId={}", histories.size(), paymentId);
        return new PageImpl<>(responseList, pageable, histories.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentHistoryDetailResponse> getAllPaymentHistory(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        startDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
        endDate = endDate != null ? endDate : LocalDateTime.now().plusMonths(1);

        log.info("전체 결제 이력 조회: startDate={}, endDate={}", startDate, endDate);

        Page<Payment> payments = paymentRepository.findByCreatedAtBetween(startDate, endDate, pageable);

        List<PaymentHistoryDetailResponse> responses = new ArrayList<>();

        for (Payment payment : payments.getContent()) {
            List<PaymentHistory> histories = paymentHistoryRepository.findByPaymentId(payment.getPaymentId());

            if (!histories.isEmpty()) {
                // 변경 횟수, 첫 생성일, 마지막 업데이트일 계산
                int historyCount = histories.size() - 1; // 초기 생성 제외한 변경 횟수
                LocalDateTime firstCreatedAt = histories.get(0).getCreatedAt();
                LocalDateTime lastUpdatedAt = histories.get(histories.size() - 1).getCreatedAt();

                responses.add(new PaymentHistoryDetailResponse(payment, historyCount, firstCreatedAt, lastUpdatedAt));
            }
        }

        log.info("결제 이력 {} 건 조회됨", responses.size());
        return new PageImpl<>(responses, pageable, payments.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentHistoryDetailResponse> getUserPaymentHistory(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        startDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
        endDate = endDate != null ? endDate : LocalDateTime.now().plusMonths(1);

        log.info("사용자 결제 이력 조회: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        // 사용자의 모든 결제 조회 (보호자 또는 간병인)
        Page<Payment> payments;

        // 보호자 조회
        payments = paymentRepository.findByGuardianIdAndCreatedAtBetween(userId, startDate, endDate, pageable);

        // 보호자로 조회한 결과가 없으면 간병인 조회
        if (payments.isEmpty()) {
            payments = paymentRepository.findByCaregiverIdAndCreatedAtBetween(userId, startDate, endDate, pageable);
        }

        List<PaymentHistoryDetailResponse> responses = new ArrayList<>();

        for (Payment payment : payments.getContent()) {
            List<PaymentHistory> histories = paymentHistoryRepository.findByPaymentId(payment.getPaymentId());

            if (!histories.isEmpty()) {
                int historyCount = histories.size() - 1;
                LocalDateTime firstCreatedAt = histories.get(0).getCreatedAt();
                LocalDateTime lastUpdatedAt = histories.get(histories.size() - 1).getCreatedAt();

                responses.add(new PaymentHistoryDetailResponse(payment, historyCount, firstCreatedAt, lastUpdatedAt));
            }
        }

        log.info("사용자 결제 이력 {} 건 조회됨: userId={}", responses.size(), userId);
        return new PageImpl<>(responses, pageable, payments.getTotalElements());
    }
}