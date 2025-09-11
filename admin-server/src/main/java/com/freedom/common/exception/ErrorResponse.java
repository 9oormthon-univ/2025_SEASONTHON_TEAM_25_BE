package com.freedom.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private String code;
    private String message;
    private LocalDateTime timestamp;
    
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(customMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    // 기존 호환성을 위해 유지
    public static ErrorResponse from(ErrorCode errorCode) {
        return of(errorCode);
    }
}
