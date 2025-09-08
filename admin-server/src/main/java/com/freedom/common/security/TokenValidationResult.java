package com.freedom.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT 토큰 검증 결과를 나타내는 클래스
 */
@Getter
@AllArgsConstructor
public class TokenValidationResult {
    
    public enum Status {
        VALID,           // 유효한 토큰
        EXPIRED,         // 만료된 토큰
        INVALID,         // 잘못된 토큰 (서명 오류, 형식 오류 등)
        WRONG_TYPE       // 잘못된 토큰 타입 (ACCESS 토큰이 아님)
    }
    
    private final Status status;
    private final String message;
    
    public static TokenValidationResult valid() {
        return new TokenValidationResult(Status.VALID, "유효한 토큰");
    }
    
    public static TokenValidationResult expired() {
        return new TokenValidationResult(Status.EXPIRED, "만료된 토큰");
    }
    
    public static TokenValidationResult invalid(String message) {
        return new TokenValidationResult(Status.INVALID, "유효하지 않은 토큰: " + message);
    }
    
    public static TokenValidationResult wrongType() {
        return new TokenValidationResult(Status.WRONG_TYPE, "잘못된 토큰 타입");
    }
    
    public boolean isValid() {
        return status == Status.VALID;
    }
    
    public boolean isExpired() {
        return status == Status.EXPIRED;
    }
    
    public boolean isInvalid() {
        return status == Status.INVALID;
    }
    
    public boolean isWrongType() {
        return status == Status.WRONG_TYPE;
    }
}
