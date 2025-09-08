package com.freedom.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ValidationErrorResponse {
    private final String code;
    private final String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
    private final List<ValidationFieldError> errors;
    
    public static ValidationErrorResponse of(String code, String message, List<ValidationFieldError> errors) {
        return ValidationErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }
    
    public static ValidationErrorResponse of(ErrorCode errorCode, List<ValidationFieldError> errors) {
        return ValidationErrorResponse.builder()
                .code(errorCode.getCode())
                .message("입력값 검증에 실패했습니다.")
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }
}
