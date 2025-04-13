package com.carenest.business.reservationservice.application.dto.request;

import com.carenest.business.reservationservice.domain.model.Gender;
import com.carenest.business.reservationservice.domain.model.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReservationCreateRequest {
    @NotNull(message = "보호자 ID는 필수 입력 항목입니다")
    private UUID guardianId;

    @NotBlank(message = "보호자 이름은 필수 입력 항목입니다")
    @Size(max = 50, message = "보호자 이름은 최대 50자까지 입력 가능합니다")
    private String guardianName;

    @NotNull(message = "간병인 ID는 필수 입력 항목입니다")
    private UUID caregiverId;

    @NotBlank(message = "간병인 이름은 필수 입력 항목입니다")
    @Size(max = 50, message = "간병인 이름은 최대 50자까지 입력 가능합니다")
    private String caregiverName;

    @NotBlank(message = "환자 이름은 필수 입력 항목입니다")
    @Size(max = 50, message = "환자 이름은 최대 50자까지 입력 가능합니다")
    private String patientName;

    @NotNull(message = "환자 나이는 필수 입력 항목입니다")
    @Positive(message = "환자 나이는 양수여야 합니다")
    private Integer patientAge;

    @NotNull(message = "환자 성별은 필수 입력 항목입니다")
    private Gender patientGender;

    @NotBlank(message = "환자 상태는 필수 입력 항목입니다")
    @Size(max = 255, message = "환자 상태는 최대 255자까지 입력 가능합니다")
    private String patientCondition;

    @NotBlank(message = "돌봄 주소는 필수 입력 항목입니다")
    @Size(max = 255, message = "돌봄 주소는 최대 255자까지 입력 가능합니다")
    private String careAddress;

    @NotNull(message = "돌봄 시작 일시는 필수 입력 항목입니다")
    private LocalDateTime startedAt;

    @NotNull(message = "돌봄 종료 일시는 필수 입력 항목입니다")
    private LocalDateTime endedAt;

    @NotNull(message = "서비스 유형은 필수 입력 항목입니다")
    private ServiceType serviceType;

    @NotBlank(message = "요청 서비스 목록은 필수 입력 항목입니다")
    @Size(max = 255, message = "요청 서비스 목록은 최대 255자까지 입력 가능합니다")
    private String serviceRequests;

    @NotNull(message = "총 금액은 필수 입력 항목입니다")
    @Positive(message = "총 금액은 양수여야 합니다")
    private BigDecimal totalAmount;

    @NotNull(message = "서비스 수수료는 필수 입력 항목입니다")
    @Positive(message = "서비스 수수료는 양수여야 합니다")
    private BigDecimal serviceFee;
}