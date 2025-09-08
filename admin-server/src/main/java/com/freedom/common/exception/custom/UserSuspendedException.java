package com.freedom.common.exception.custom;

public class UserSuspendedException extends RuntimeException {
    
    public UserSuspendedException() {
        super("정지된 사용자입니다.");
    }
    
    public UserSuspendedException(String email) {
        super("정지된 사용자입니다. email: " + email);
    }
}
