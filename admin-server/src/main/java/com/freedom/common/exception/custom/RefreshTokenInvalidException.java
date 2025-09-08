package com.freedom.common.exception.custom;

public class RefreshTokenInvalidException extends RuntimeException {
    
    public RefreshTokenInvalidException() {
        super("유효하지 않은 리프레시 토큰입니다.");
    }
    
    public RefreshTokenInvalidException(String reason) {
        super("유효하지 않은 리프레시 토큰입니다. 사유: " + reason);
    }
}
