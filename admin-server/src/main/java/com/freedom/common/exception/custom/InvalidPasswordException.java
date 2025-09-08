package com.freedom.common.exception.custom;

public class InvalidPasswordException extends RuntimeException {
    
    public InvalidPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }
    
    public InvalidPasswordException(String email) {
        super("비밀번호가 일치하지 않습니다. email: " + email);
    }
}
