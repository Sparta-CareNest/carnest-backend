package com.carenest.business.userservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.userservice.application.dto.request.LoginRequestDTO;
import com.carenest.business.userservice.application.dto.request.SignupRequestDTO;
import com.carenest.business.userservice.application.dto.request.UpdateUserRequestDTO;
import com.carenest.business.userservice.application.dto.response.*;
import com.carenest.business.userservice.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseDto<SignupResponseDTO> signup(@RequestBody SignupRequestDTO request) {
        SignupResponseDTO response = userService.signup(request);
        return ResponseDto.success("회원가입이 완료되었습니다.", response);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseDto<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = userService.login(request);
        return ResponseDto.success("로그인이 완료되었습니다.", response);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseDto<Void> logout() {
        userService.logout();
        return ResponseDto.success("로그아웃이 완료되었습니다.", null);
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseDto<UserInfoResponseDTO> getMyInfo() {
        UserInfoResponseDTO response = userService.getMyInfo();
        return ResponseDto.success(response);
    }

    // 내 정보 수정
    @PutMapping("/me")
    public ResponseDto<UpdateUserResponseDTO> updateMyInfo(@RequestBody UpdateUserRequestDTO request) {
        UpdateUserResponseDTO response = userService.updateMyInfo(request);
        return ResponseDto.success("정보가 수정되었습니다.", response);
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseDto<WithdrawalResponseDTO> deleteMyAccount() {
        WithdrawalResponseDTO response = userService.deleteMyAccount();
        return ResponseDto.success("탈퇴가 완료되었습니다.", response);
    }

    // 간병인 프로필 조회 (보호자용)
    @GetMapping("/caregivers/profile")
    public ResponseDto<CaregiverProfileResponseDTO> getCaregiverProfile() {
        CaregiverProfileResponseDTO response = userService.getCaregiverProfile();
        return ResponseDto.success(response);
    }
}