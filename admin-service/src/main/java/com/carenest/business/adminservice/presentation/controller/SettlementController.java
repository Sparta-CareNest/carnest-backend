package com.carenest.business.adminservice.presentation.controller;

import com.carenest.business.adminservice.application.dto.request.SettlementRequestDto;
import com.carenest.business.adminservice.application.dto.response.SettlementResponseDto;
import com.carenest.business.adminservice.application.service.SettlementService;
import com.carenest.business.adminservice.exception.SettlementAccessDeniedException;
import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import com.carenest.business.common.model.UserRole;
import com.carenest.business.common.response.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/settlements")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    // 관리자 권한 체크 메서드
    private void checkAdmin(AuthUserInfo authUserInfo) {
        if (!"ADMIN".equals(authUserInfo.getRole())) {
            throw new SettlementAccessDeniedException("관리자만 접근 가능합니다.");
        }
    }

    // 정산 생성
    @Operation(summary = "정산 생성", description = "관리자가 수동 정산 생성하는 API입니다")
    @PostMapping
    public ResponseDto<SettlementResponseDto> createSettlement(
            @Valid @RequestBody SettlementRequestDto requestDto,
            @AuthUser AuthUserInfo authUserInfo) {
        checkAdmin(authUserInfo);
        SettlementResponseDto responseDto = settlementService.createSettlement(
                requestDto.getCareWorkerId(),
                requestDto.getAmount(),
                requestDto.getPeriodStart(),
                requestDto.getPeriodEnd()
        );

        return ResponseDto.success("정산 생성 완료", responseDto);
    }

    // 특정 간병인의 정산 내역 조회
    @Operation(summary = "정산 내역 조회", description = "특정 간병인의 정산 내역을 조회하는 API입니다")
    @GetMapping("/{careWorkerId}")
    public ResponseDto<List<SettlementResponseDto>> getSettlementsByCareWorkerId(
            @PathVariable UUID careWorkerId,
            @AuthUser AuthUserInfo authUserInfo) {
        checkAdmin(authUserInfo);
        List<SettlementResponseDto> settlements = settlementService.getSettlementsByCareWorkerId(careWorkerId);
        return ResponseDto.success("정산 내역 조회 완료", settlements);
    }

    // 결제 내역 기반 정산 생성
    @Operation(summary = "결제 내역 기반 정산 생성", description = "결제 내역 기반으로 관리자가 정산을 생성하는 API입니다")
    @PostMapping("/create-from-payment")
    public ResponseDto<SettlementResponseDto> createSettlementFromPayment(
            @AuthUser AuthUserInfo authUserInfo,
            @RequestParam UUID careWorkerId,
            @RequestParam LocalDate periodStart,
            @RequestParam LocalDate periodEnd) {
        checkAdmin(authUserInfo);

        SettlementResponseDto responseDto = settlementService.createSettlementFromPaymentData(
                careWorkerId, periodStart, periodEnd
        );

        return ResponseDto.success("정산 생성 완료", responseDto);
    }
}
