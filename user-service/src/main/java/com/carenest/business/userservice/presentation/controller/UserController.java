package com.carenest.business.userservice.presentation.controller;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.userservice.application.dto.request.LoginRequestDTO;
import com.carenest.business.userservice.application.dto.request.SignupRequestDTO;
import com.carenest.business.userservice.application.dto.request.UpdateUserRequestDTO;
import com.carenest.business.userservice.application.dto.response.*;
import com.carenest.business.userservice.application.service.UserService;
import com.carenest.business.common.annotation.AuthUser;
import com.carenest.business.common.annotation.AuthUserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "회원가입을 수행합니다.")
    @PostMapping("/signup")
    public ResponseDto<SignupResponseDTO> signup(@RequestBody SignupRequestDTO request) {
        SignupResponseDTO response = userService.signup(request);
        return ResponseDto.success("회원가입이 완료되었습니다.", response);
    }

    @Operation(summary = "로그인", description = "로그인을 수행하고 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseDto<LoginResponseDTO> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        LoginResponseDTO loginResponse = userService.login(request, response);
        return ResponseDto.success("로그인이 완료되었습니다.", loginResponse);
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 수행하고 토큰을 블랙리스트 처리합니다.")
    @PostMapping("/logout")
    public ResponseDto<Void> logout(@AuthUser AuthUserInfo authUserInfo, HttpServletRequest request) {
        userService.logout(authUserInfo, request);
        return ResponseDto.success("로그아웃이 완료되었습니다.", null);
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 유저 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseDto<UserInfoResponseDTO> getMyInfo(@AuthUser AuthUserInfo authUserInfo) {
        UserInfoResponseDTO response = userService.getMyInfo(authUserInfo);
        return ResponseDto.success("내 정보 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인된 유저의 정보를 수정합니다.")
    @PatchMapping("/me")
    public ResponseDto<UpdateUserResponseDTO> updateMyInfo(@AuthUser AuthUserInfo authUserInfo,
                                                           @RequestBody UpdateUserRequestDTO updateUserRequestDTO) {
        UpdateUserResponseDTO response = userService.updateMyInfo(authUserInfo, updateUserRequestDTO);
        return ResponseDto.success("정보가 수정되었습니다.", response);
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인된 유저의 계정을 삭제합니다.")
    @DeleteMapping("/me")
    public ResponseDto<Void> deleteMyAccount(@AuthUser AuthUserInfo authUserInfo,
                                             HttpServletRequest request) {
        userService.deleteMyAccount(authUserInfo, request);
        return ResponseDto.success("탈퇴가 완료되었습니다.", null);
    }
}