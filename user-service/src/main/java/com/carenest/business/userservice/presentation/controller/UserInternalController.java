package com.carenest.business.userservice.presentation.controller;

import java.util.UUID;

import com.carenest.business.common.response.ResponseDto;
import com.carenest.business.userservice.application.dto.response.UserInfoResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carenest.business.userservice.application.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/users")
public class UserInternalController {
	private final UserService userService;

	// 유저가 존재하는지 확인하는 API
	@GetMapping("/{id}")
	public Boolean isExistedCaregiver(@PathVariable UUID id) {
		return userService.existsById(id);
	}

	// 유저 상세 정보 조회 API
	@GetMapping("/{id}/details")
	public ResponseDto<UserInfoResponseDTO> getUserDetails(@PathVariable UUID id) {
		UserInfoResponseDTO userInfo = userService.getUserDetails(id);
		return ResponseDto.success("사용자 정보 조회 성공", userInfo);
	}

}
