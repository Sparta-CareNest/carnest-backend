package com.carenest.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * API 응답을 공통 포맷으로 감싸는 클래스
 */
@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {
    private final int code;
    private final String status;
    private final String message;
    private final T data;

    /**
     * 요청이 성공했을 때 사용하는 static 팩토리 메서드
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, "success", message, data);
    }

    /**
     * 요청이 성공했지만 커스텀 메시지를 전달하고 싶을 때
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", "요청이 성공했습니다.", data);
    }

    /**
     * 요청이 실패했을 때 사용하는 static 팩토리 메서드
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, "error", message, null);
    }
}