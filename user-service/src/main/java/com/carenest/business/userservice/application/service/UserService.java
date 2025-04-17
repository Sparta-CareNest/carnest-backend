package com.carenest.business.userservice.application.service;

import java.util.UUID;

import com.carenest.business.common.exception.BaseException;
import com.carenest.business.common.exception.CommonErrorCode;
import com.carenest.business.common.model.UserRole;
import com.carenest.business.userservice.application.dto.request.LoginRequestDTO;
import com.carenest.business.userservice.application.dto.request.SignupRequestDTO;
import com.carenest.business.userservice.application.dto.request.UpdateUserRequestDTO;
import com.carenest.business.userservice.application.dto.response.*;
import com.carenest.business.userservice.domain.exception.UserErrorCode;
import com.carenest.business.userservice.domain.model.User;
import com.carenest.business.userservice.domain.repository.UserRepository;
import com.carenest.business.userservice.infrastructure.security.JwtUtil;
import com.carenest.business.common.annotation.AuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    public SignupResponseDTO signup(SignupRequestDTO request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BaseException(UserErrorCode.DUPLICATED_EMAIL);
        }

        // 아이디 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BaseException(UserErrorCode.DUPLICATED_USERNAME);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        request.setPassword(encodedPassword);

        // 회원 정보 저장
        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .email(request.getEmail())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);
        return SignupResponseDTO.from(savedUser);
    }

    // 로그인
    public LoginResponseDTO login(LoginRequestDTO request, HttpServletResponse response) {
        // 사용자명으로 사용자 찾기
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BaseException(UserErrorCode.USERNAME_NOT_FOUND));

        // 비밀번호 일치 확인 (암호화된 비밀번호 비교)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BaseException(UserErrorCode.INVALID_PASSWORD);
        }

        // JWT 생성
        String accessToken = jwtUtil.createToken(user.getUserId(), user.getUsername(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.createToken(user.getUserId(), user.getUsername(), user.getEmail(), user.getRole());

        return LoginResponseDTO.of(accessToken,refreshToken, user);
    }

//    // 로그아웃
//    @Transactional
//    public void logout() {
//        // 현재 인증된 사용자 정보 가져오기 (실제로는 SecurityContext에서 가져옴)
//        String currentUsername = "현재_로그인한_사용자"; // 예시
//
//        // 토큰 무효화 로직 (실제로는 Redis에 블랙리스트 저장 등)
//        System.out.println(currentUsername + "의 토큰이 무효화되었습니다.");
//    }
//
    // 내 정보 조회
    public UserInfoResponseDTO getMyInfo(AuthUserInfo authUserInfo) {

        User user = userRepository.findById(authUserInfo.getUserId())
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        return UserInfoResponseDTO.from(user);
    }

    // 내 정보 수정
    @Transactional
    public UpdateUserResponseDTO updateMyInfo(AuthUserInfo authUserInfo,
                                               UpdateUserRequestDTO request) {

        User user = userRepository.findById(authUserInfo.getUserId())
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        User updated = user.toBuilder()
                .nickname(request.getNickname() != null ? request.getNickname() : user.getNickname())
                .email(request.getEmail() != null ? request.getEmail() : user.getEmail())
                .name(request.getName() != null ? request.getName() : user.getName())
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber() : user.getPhoneNumber())
                .build();

        User updatedUser = userRepository.save(updated);

        return UpdateUserResponseDTO.from(updatedUser);
    }

	public Boolean existsById(UUID id) {
        return userRepository.existsById(id);
	}


    // 회원 탈퇴
    @Transactional
    public void deleteMyAccount(AuthUserInfo authUserInfo) {
        User user = userRepository.findById(authUserInfo.getUserId())
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        user.softDelete();
        userRepository.save(user);
    }
}