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
import com.carenest.business.paymentservice.infrastructure.client.ReservationInternalClient;
import com.carenest.business.paymentservice.infrastructure.client.UserInternalClient;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.ReservationDetailsResponseDto;
import com.carenest.business.paymentservice.infrastructure.client.dto.response.UserInfoResponseDTO;
import com.carenest.business.paymentservice.infrastructure.external.PaymentGatewayService;
import com.carenest.business.paymentservice.infrastructure.kafka.PaymentEventProducer;
import com.carenest.business.paymentservice.infrastructure.repository.PaymentHistoryRepository;
import com.carenest.business.paymentservice.infrastructure.repository.PaymentRepository;
import com.carenest.business.paymentservice.infrastructure.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentDomainService paymentDomainService;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;
    private final PaymentEventProducer paymentEventProducer;
    private final ReservationInternalClient reservationInternalClient;
    private final UserInternalClient userInternalClient;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request, UUID guardianId) {
        log.info("결제 생성 요청: reservationId={}, amount={}", request.getReservationId(), request.getAmount());

        // 동일한 예약에 대해 결제가 존재하는지 확인
        Optional<Payment> existingPayment = paymentRepository.findByReservationId(request.getReservationId());
        if (existingPayment.isPresent()) {
            if (existingPayment.get().getStatus() == PaymentStatus.PENDING) {
                log.info("이미 진행 중인 결제가 있습니다: paymentId={}", existingPayment.get().getPaymentId());
                return new PaymentResponse(existingPayment.get(), getUserDetails(guardianId), getUserDetails(request.getCaregiverId()), getReservationDetails(request.getReservationId()));
            }
            log.warn("이미 처리된 결제가 있습니다: paymentId={}, status={}",
                    existingPayment.get().getPaymentId(), existingPayment.get().getStatus());
            throw new DuplicatePaymentException();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 토스페이먼츠 결제 준비 요청
            Map<String, Object> paymentPrepareResult = paymentGatewayService.preparePayment(
                    request.getReservationId(),
                    request.getAmount(),
                    request.getPaymentMethod()
            );

            // 결제 정보 생성
            Payment payment = new Payment(
                    request.getReservationId(),
                    guardianId,
                    request.getCaregiverId(),
                    request.getAmount(),
                    request.getPaymentMethod(),
                    request.getPaymentMethodDetail() != null ? objectMapper.writeValueAsString(request.getPaymentMethodDetail()) : null,
                    "TOSS_PAYMENTS" // 토스페이먼츠 결제 게이트웨이 사용
            );

            // 결제 저장
            Payment savedPayment = paymentRepository.save(payment);
            paymentDomainService.createPaymentHistory(savedPayment);

            log.info("결제 생성 완료: paymentId={}", savedPayment.getPaymentId());

            UserInfoResponseDTO guardianDetails = getUserDetails(guardianId);
            UserInfoResponseDTO caregiverDetails = getUserDetails(request.getCaregiverId());
            ReservationDetailsResponseDto reservationDetails = getReservationDetails(request.getReservationId());

            PaymentResponse response = new PaymentResponse(savedPayment, guardianDetails, caregiverDetails, reservationDetails);
            response.setTossPaymentsInfo(paymentPrepareResult);

            return response;
        } catch (Exception e) {
            log.error("결제 생성 중 오류 발생", e);
            if (e instanceof PaymentException) {
                throw (PaymentException) e;
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

        UserInfoResponseDTO guardianDetails = getUserDetails(payment.getGuardianId());
        UserInfoResponseDTO caregiverDetails = getUserDetails(payment.getCaregiverId());
        ReservationDetailsResponseDto reservationDetails = getReservationDetails(payment.getReservationId());

        return new PaymentResponse(payment, guardianDetails, caregiverDetails, reservationDetails);
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

        UserInfoResponseDTO guardianDetails = getUserDetails(payment.getGuardianId());
        UserInfoResponseDTO caregiverDetails = getUserDetails(payment.getCaregiverId());
        ReservationDetailsResponseDto reservationDetails = getReservationDetails(payment.getReservationId());

        return new PaymentResponse(payment, guardianDetails, caregiverDetails, reservationDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentListResponse> getPaymentList(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        startDate = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
        endDate = endDate != null ? endDate : LocalDateTime.now().plusMonths(1);

        log.info("결제 목록 조회: startDate={}, endDate={}, page={}, size={}",
                startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());

        Page<Payment> payments = paymentRepository.findByCreatedAtBetween(startDate, endDate, pageable);

        return payments.map(payment -> {
            PaymentListResponse response = new PaymentListResponse(payment);

            // 사용자 및 예약 정보 설정
            try {
                ReservationDetailsResponseDto reservationDetails = getReservationDetails(payment.getReservationId());
                UserInfoResponseDTO caregiverDetails = getUserDetails(payment.getCaregiverId());

                response.setCaregiverName(caregiverDetails.getName());
                if (reservationDetails != null) {
                    response.setServicePeriod(formatServicePeriod(
                            reservationDetails.getStartedAt(),
                            reservationDetails.getEndedAt()
                    ));
                }
            } catch (Exception e) {
                log.warn("결제 목록 조회 중 사용자/예약 정보 가져오기 실패: {}", e.getMessage());
            }

            return response;
        });
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
            return transformPaymentsToResponse(paymentsByGuardian);
        }

        // 간병인 ID로 조회
        Page<Payment> paymentsByCaregiver = paymentRepository.findByCaregiverIdAndCreatedAtBetween(
                userId, startDate, endDate, pageable);

        log.info("간병인으로 결제 내역 {} 건 조회됨", paymentsByCaregiver.getTotalElements());
        return transformPaymentsToResponse(paymentsByCaregiver);
    }

    @Override
    @Transactional
    public PaymentResponse completePayment(UUID paymentId, PaymentCompleteRequest request) {
        log.info("결제 완료 처리: paymentId={}, paymentKey={}", paymentId, request.getPaymentKey());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("결제 정보를 찾을 수 없습니다: paymentId={}", paymentId);
                    return new PaymentNotFoundException();
                });

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("유효하지 않은 결제 상태: paymentId={}, status={}", paymentId, payment.getStatus());
            throw new InvalidPaymentStatusException();
        }

        try {
            PaymentCompleteRequest actualResult;
            if (request.getPaymentKey() != null) {
                // 토스페이먼츠 결제 승인 요청
                actualResult = paymentGatewayService.approvePayment(
                        request.getPaymentKey(), payment.getAmount());
            } else {
                actualResult = request;
            }

            // 결제 완료 처리
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

            // 결제 완료 이벤트
            try {
                paymentEventProducer.sendPaymentCompletedEvent(savedPayment);
                log.info("결제 완료 이벤트 발행 완료: paymentId={}, reservationId={}",
                        savedPayment.getPaymentId(), savedPayment.getReservationId());
            } catch (Exception e) {
                log.error("결제 완료 이벤트 발행 실패: paymentId={}, 에러={}", paymentId, e.getMessage(), e);
                // 이벤트 발행 실패는 결제 완료 자체에 영향을 주지 않도록 예외 처리
            }

            log.info("결제 완료 처리 성공: paymentId={}, reservationId={}",
                    paymentId, payment.getReservationId());

            UserInfoResponseDTO guardianDetails = getUserDetails(payment.getGuardianId());
            UserInfoResponseDTO caregiverDetails = getUserDetails(payment.getCaregiverId());
            ReservationDetailsResponseDto reservationDetails = getReservationDetails(payment.getReservationId());

            return new PaymentResponse(savedPayment, guardianDetails, caregiverDetails, reservationDetails);
        } catch (Exception e) {
            log.error("결제 완료 처리 중 오류 발생: {}", e.getMessage(), e);
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
            // 토스페이먼츠 결제 취소 요청 (COMPLETED 상태일 때만)
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

            // 사용자 및 예약 정보 조회
            UserInfoResponseDTO guardianDetails = getUserDetails(payment.getGuardianId());
            UserInfoResponseDTO caregiverDetails = getUserDetails(payment.getCaregiverId());
            ReservationDetailsResponseDto reservationDetails = getReservationDetails(payment.getReservationId());

            return new PaymentResponse(savedPayment, guardianDetails, caregiverDetails, reservationDetails);
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
            // 토스페이먼츠 결제 환불 요청
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

            UserInfoResponseDTO guardianDetails = getUserDetails(payment.getGuardianId());
            UserInfoResponseDTO caregiverDetails = getUserDetails(payment.getCaregiverId());
            ReservationDetailsResponseDto reservationDetails = getReservationDetails(payment.getReservationId());

            return new PaymentResponse(savedPayment, guardianDetails, caregiverDetails, reservationDetails);
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

        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        String guardianName = "알 수 없음";
        String caregiverName = "알 수 없음";

        if (payment != null) {
            try {
                UserInfoResponseDTO guardianDetails = getUserDetails(payment.getGuardianId());
                if (guardianDetails != null) {
                    guardianName = guardianDetails.getName();
                }

                UserInfoResponseDTO caregiverDetails = getUserDetails(payment.getCaregiverId());
                if (caregiverDetails != null) {
                    caregiverName = caregiverDetails.getName();
                }
            } catch (Exception e) {
                log.warn("사용자 정보 조회 실패: {}", e.getMessage());
            }
        }

        List<PaymentHistoryResponse> responseList = new ArrayList<>();
        List<PaymentHistory> pagedHistories = histories.subList(start, end);

        // 이전 상태 추적
        PaymentStatus prevStatus = null;

        for (int i = 0; i < pagedHistories.size(); i++) {
            PaymentHistory history = pagedHistories.get(i);

            // 첫 번째가 아닌 경우 이전 이력의 상태를 이전 상태로 설정
            if (i > 0) {
                prevStatus = pagedHistories.get(i - 1).getStatus();
            }

            // 사용자 정보와 이전 상태 정보를 포함하여 응답 생성
            PaymentHistoryResponse response = new PaymentHistoryResponse(
                    history, guardianName, caregiverName, prevStatus);

            responseList.add(response);
        }

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

                PaymentHistoryDetailResponse response = new PaymentHistoryDetailResponse(payment, historyCount, firstCreatedAt, lastUpdatedAt);

                // 사용자 및 예약 정보 설정
                try {
                    UserInfoResponseDTO caregiverDetails = getUserDetails(payment.getCaregiverId());
                    ReservationDetailsResponseDto reservationDetails = getReservationDetails(payment.getReservationId());

                    if (caregiverDetails != null) {
                        response.setCaregiverName(caregiverDetails.getName());
                    }

                    if (reservationDetails != null) {
                        response.setServicePeriod(formatServicePeriod(
                                reservationDetails.getStartedAt(),
                                reservationDetails.getEndedAt()
                        ));
                    }
                } catch (Exception e) {
                    log.warn("결제 이력 조회 중 사용자/예약 정보 가져오기 실패: {}", e.getMessage());
                }

                responses.add(response);
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

                PaymentHistoryDetailResponse response = new PaymentHistoryDetailResponse(payment, historyCount, firstCreatedAt, lastUpdatedAt);

                // 사용자 및 예약 정보 설정
                try {
                    UserInfoResponseDTO caregiverDetails = getUserDetails(payment.getCaregiverId());
                    ReservationDetailsResponseDto reservationDetails = getReservationDetails(payment.getReservationId());

                    if (caregiverDetails != null) {
                        response.setCaregiverName(caregiverDetails.getName());
                    }

                    if (reservationDetails != null) {
                        response.setServicePeriod(formatServicePeriod(
                                reservationDetails.getStartedAt(),
                                reservationDetails.getEndedAt()
                        ));
                    }
                } catch (Exception e) {
                    log.warn("결제 이력 조회 중 사용자/예약 정보 가져오기 실패: {}", e.getMessage());
                }

                responses.add(response);
            }
        }

        log.info("사용자 결제 이력 {} 건 조회됨: userId={}", responses.size(), userId);
        return new PageImpl<>(responses, pageable, payments.getTotalElements());
    }

    private Page<PaymentListResponse> transformPaymentsToResponse(Page<Payment> payments) {
        return payments.map(payment -> {
            PaymentListResponse response = new PaymentListResponse(payment);

            // 사용자 및 예약 정보 설정
            try {
                UserInfoResponseDTO caregiverDetails = getUserDetails(payment.getCaregiverId());
                ReservationDetailsResponseDto reservationDetails = getReservationDetails(payment.getReservationId());

                if (caregiverDetails != null) {
                    response.setCaregiverName(caregiverDetails.getName());
                }

                if (reservationDetails != null) {
                    response.setServicePeriod(formatServicePeriod(
                            reservationDetails.getStartedAt(),
                            reservationDetails.getEndedAt()
                    ));
                }
            } catch (Exception e) {
                log.warn("결제 목록 변환 중 사용자/예약 정보 가져오기 실패: {}", e.getMessage());
            }

            return response;
        });
    }

    private UserInfoResponseDTO getUserDetails(UUID userId) {
        try {
            // userInternalClient를 통해 사용자 정보 조회
            return userInternalClient.getUserDetails(userId);
        } catch (Exception e) {
            log.warn("사용자 정보 조회 실패: userId={}, error={}", userId, e.getMessage());
            return new UserInfoResponseDTO(userId, "알 수 없음", "사용자", "이메일 없음", "알 수 없음", "전화번호 없음");
        }
    }

    private ReservationDetailsResponseDto getReservationDetails(UUID reservationId) {
        try {
            // reservationInternalClient를 통해 예약 정보 조회
            return reservationInternalClient.getReservationDetails(reservationId);
        } catch (Exception e) {
            log.warn("예약 정보 조회 실패: reservationId={}, error={}", reservationId, e.getMessage());
            return null;
        }
    }

    private String formatServicePeriod(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return "기간 정보 없음";
        }

        return start.toLocalDate() + " ~ " + end.toLocalDate();
    }
}