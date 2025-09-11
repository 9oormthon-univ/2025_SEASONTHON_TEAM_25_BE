package com.freedom.common.exception.custom;

public class TokenInvalidException extends RuntimeException {
    
    public TokenInvalidException() {
        super("유효하지 않은 토큰입니다.");
    }
    
    public TokenInvalidException(String reason) {
        super("유효하지 않은 토큰입니다. 사유: " + reason);
    }
}
