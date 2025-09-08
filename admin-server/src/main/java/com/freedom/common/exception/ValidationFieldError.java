package com.freedom.common.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValidationFieldError {
    private final String field;
    private final Object rejectedValue;
    private final String code;
    private final String message;
    
    public static ValidationFieldError of(String field, Object rejectedValue, String code, String message) {
        return ValidationFieldError.builder()
                .field(field)
                .rejectedValue(rejectedValue)
                .code(code)
                .message(message)
                .build();
    }
}
