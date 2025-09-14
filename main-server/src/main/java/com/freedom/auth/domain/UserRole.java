package com.freedom.auth.domain;

import lombok.Getter;

/**
 * 사용자 권한 열거형
 */
@Getter
public enum UserRole {
    
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");
    
    private final String authority;
    private final String description;
    
    UserRole(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }
}
