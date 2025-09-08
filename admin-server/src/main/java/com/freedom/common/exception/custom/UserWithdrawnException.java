package com.freedom.common.exception.custom;

public class UserWithdrawnException extends RuntimeException {
    
    public UserWithdrawnException() {
        super("탈퇴한 사용자입니다.");
    }
    
    public UserWithdrawnException(String email) {
        super("탈퇴한 사용자입니다. email: " + email);
    }
}
