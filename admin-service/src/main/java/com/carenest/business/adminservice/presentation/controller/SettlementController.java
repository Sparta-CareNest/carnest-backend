package com.carenest.business.adminservice.presentation.controller;

import com.carenest.business.adminservice.application.dto.request.SettlementRequestDto;
import com.carenest.business.adminservice.application.dto.response.SettlementResponseDto;
import com.carenest.business.adminservice.application.service.SettlementService;
import com.carenest.business.common.response.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/settlements")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    // 정산 생성
    @PostMapping
    public ResponseDto<SettlementResponseDto> createSettlement(@Valid @RequestBody SettlementRequestDto requestDto) {
        SettlementResponseDto responseDto = settlementService.createSettlement(
                requestDto.getCareWorkerId(),
                requestDto.getAmount(),
                requestDto.getPeriodStart(),
                requestDto.getPeriodEnd()
        );

        return ResponseDto.success("정산 생성 완료", responseDto);
    }

    // 특정 간병인의 정산 내역 조회
    @GetMapping("/{careWorkerId}")
    public ResponseDto<List<SettlementResponseDto>> getSettlementsByCareWorkerId(@PathVariable UUID careWorkerId) {
        List<SettlementResponseDto> settlements = settlementService.getSettlementsByCareWorkerId(careWorkerId);
        return ResponseDto.success("정산 내역 조회 완료", settlements);
    }
}
