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
public class SuccessResponse {

    private String code;
    private String message;
    private LocalDateTime timestamp;

    public static SuccessResponse ok(String message) {
        return SuccessResponse.builder()
                .code("OK")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}


