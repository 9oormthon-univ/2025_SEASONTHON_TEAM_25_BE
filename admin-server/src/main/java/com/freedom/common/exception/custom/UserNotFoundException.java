package com.freedom.common.exception.custom;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException() {
        super("사용자를 찾을 수 없습니다.");
    }
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다. userId: " + userId);
    }
    
    public UserNotFoundException(String field, String value) {
        super("사용자를 찾을 수 없습니다. " + field + ": " + value);
    }
}
