package com.carenest.business.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResponseDto<T> {
    private final int code;
    private final String status;
    private final String message;
    private final T data;

    // 요청이 성공했을 때 커스텀 메시지
    public static <T> ResponseDto<T> success(String message, T data) {
        return new ResponseDto<>(200, "success", message, data);
    }

    // 요청이 성공했을 때
    public static <T> ResponseDto<T> success(T data) {
        return new ResponseDto<>(200, "success", "요청이 성공했습니다.", data);
    }

    // 요청이 실패했을 때 커스텀 메시지
    public static <T> ResponseDto<T> error(int code, String message) {
        return new ResponseDto<>(code, "error", message, null);
    }
}