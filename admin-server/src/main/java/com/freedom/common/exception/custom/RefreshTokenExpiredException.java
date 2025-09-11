package com.freedom.common.exception.custom;

public class RefreshTokenExpiredException extends RuntimeException {
    
    public RefreshTokenExpiredException() {
        super("리프레시 토큰이 만료되었습니다.");
    }
    
    public RefreshTokenExpiredException(String token) {
        super("리프레시 토큰이 만료되었습니다.");
    }
}
