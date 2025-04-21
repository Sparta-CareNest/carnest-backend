package com.carenest.business.userservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.userservice.application.dto.request.LoginRequestDTO;
import com.carenest.business.userservice.application.dto.request.SignupRequestDTO;
import com.carenest.business.userservice.application.dto.request.UpdateUserRequestDTO;
import com.carenest.business.userservice.application.dto.response.*;
import com.carenest.business.userservice.application.service.UserService;
import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

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
    public ResponseDto<LoginResponseDTO> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        LoginResponseDTO loginResponse = userService.login(request, response);
        return ResponseDto.success("로그인이 완료되었습니다.", loginResponse);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseDto<Void> logout(@AuthUser AuthUserInfo authUserInfo, HttpServletRequest request) {
        userService.logout(authUserInfo,request);
        return ResponseDto.success("로그아웃이 완료되었습니다.", null);
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseDto<UserInfoResponseDTO> getMyInfo(@AuthUser AuthUserInfo authUserInfo) {
        UserInfoResponseDTO response = userService.getMyInfo(authUserInfo);
        return ResponseDto.success("내 정보 조회가 완료되었습니다.",response);
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ResponseDto<UpdateUserResponseDTO> updateMyInfo(@AuthUser AuthUserInfo authUserInfo,
                                                            @RequestBody UpdateUserRequestDTO updateUserRequestDTO) {

        UpdateUserResponseDTO response = userService.updateMyInfo(authUserInfo, updateUserRequestDTO);
        return ResponseDto.success("정보가 수정되었습니다.", response);
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseDto<Void> deleteMyAccount(@AuthUser AuthUserInfo authUserInfo,
                                             HttpServletRequest request) {
        userService.deleteMyAccount(authUserInfo, request);
        return ResponseDto.success("탈퇴가 완료되었습니다.", null);
    }
}