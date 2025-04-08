package com.carenest.business.userservice.presentation.controller;

import com.carenest.business.userservice.application.dto.request.LoginRequestDTO;
import com.carenest.business.userservice.application.dto.request.SignupRequestDTO;
import com.carenest.business.userservice.application.dto.request.UpdateUserRequestDTO;
import com.carenest.business.userservice.application.dto.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDTO>> signup(@RequestBody SignupRequestDTO request) {
        SignupResponseDTO response = null;
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = null;
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다.", null));
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponseDTO>> getMyInfo() {
        UserInfoResponseDTO response = new UserInfoResponseDTO("email@example.com", "닉네임", "010-1234-5678");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UpdateUserResponseDTO>> updateMyInfo(@RequestBody UpdateUserRequestDTO request) {
        UpdateUserResponseDTO response = null;
        return ResponseEntity.ok(ApiResponse.success("정보가 수정되었습니다.", response));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<WithdrawalResponseDTO>> deleteMyAccount() {
        WithdrawalResponseDTO response = null;
        return ResponseEntity.ok(ApiResponse.success("탈퇴가 완료되었습니다.", response));
    }

    // 간병인 프로필 조회 (보호자용)
    @GetMapping("/caregivers/profile")
    public ResponseEntity<ApiResponse<CaregiverProfileResponseDTO>> getCaregiverProfile() {
        CaregiverProfileResponseDTO response = null;
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}