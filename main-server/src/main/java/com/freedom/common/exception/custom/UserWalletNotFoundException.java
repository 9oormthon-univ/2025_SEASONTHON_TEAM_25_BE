package com.freedom.common.exception.custom;

public class UserWalletNotFoundException extends RuntimeException {
    
    public UserWalletNotFoundException() {
        super("사용자 지갑을 찾을 수 없습니다.");
    }
    
    public UserWalletNotFoundException(String message) {
        super(message);
    }
    
    public UserWalletNotFoundException(Long userId) {
        super("사용자 지갑을 찾을 수 없습니다. userId: " + userId);
    }
}
