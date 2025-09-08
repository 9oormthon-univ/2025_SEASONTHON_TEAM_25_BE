package com.freedom.common.exception.custom;

public class TokenExpiredException extends RuntimeException {
    
    public TokenExpiredException() {
        super("토큰이 만료되었습니다.");
    }
    
    public TokenExpiredException(String tokenType) {
        super(tokenType + " 토큰이 만료되었습니다.");
    }
}
