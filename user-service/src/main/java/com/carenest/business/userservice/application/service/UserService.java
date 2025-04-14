package com.carenest.business.userservice.application.service;

import com.carenest.business.userservice.application.dto.request.LoginRequestDTO;
import com.carenest.business.userservice.application.dto.request.SignupRequestDTO;
import com.carenest.business.userservice.application.dto.request.UpdateUserRequestDTO;
import com.carenest.business.userservice.application.dto.response.*;
import com.carenest.business.userservice.domain.model.User;
import com.carenest.business.userservice.domain.model.UserRoleEnum;
import com.carenest.business.userservice.domain.repository.UserRepository;
import com.carenest.business.userservice.infrastructure.security.JwtUtil;
import com.carenest.business.common.annotation.AuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    public SignupResponseDTO signup(SignupRequestDTO request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 아이디 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
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
                .role(UserRoleEnum.USER)
                .build();

        User savedUser = userRepository.save(user);
        return SignupResponseDTO.from(savedUser);
    }

    // 로그인
    public LoginResponseDTO login(LoginRequestDTO request, HttpServletResponse response) {
        // 사용자명으로 사용자 찾기
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자명입니다."));

        // 비밀번호 일치 확인 (암호화된 비밀번호 비교)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return UserInfoResponseDTO.from(user);
    }

    // 내 정보 수정
    @Transactional
    public UpdateUserResponseDTO updateMyInfo(AuthUserInfo authUserInfo,
                                               UpdateUserRequestDTO request) {

        User user = userRepository.findById(authUserInfo.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        User updated = user.toBuilder()
                .nickname(request.getNickname() != null ? request.getNickname() : user.getNickname())
                .email(request.getEmail() != null ? request.getEmail() : user.getEmail())
                .name(request.getName() != null ? request.getName() : user.getName())
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber() : user.getPhoneNumber())
                .build();

        User updatedUser = userRepository.save(updated);

        return UpdateUserResponseDTO.from(updatedUser);
    }
//
//    // 회원 탈퇴
//    @Transactional
//    public WithdrawalResponseDTO deleteMyAccount() {
//        // 현재 인증된 사용자 정보 가져오기
//        String currentUsername = "현재_로그인한_사용자"; // 예시
//
//        User user = userRepository.findByUsername(currentUsername)
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//
//        // 소프트 딜리트 처리
//        // 실제 구현 시 User 클래스에 deactivate 메서드 추가 필요
//        // 또는 BaseEntity에 deletedAt 필드가 있다고 가정
//        user.setDeletedAt(java.time.LocalDateTime.now());
//        userRepository.save(user);
//
//        return new WithdrawalResponseDTO(true, "회원 탈퇴가 완료되었습니다.");
//    }
}