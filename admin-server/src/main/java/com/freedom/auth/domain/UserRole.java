package com.freedom.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 권한 열거형
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {
    
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");
    
    private final String authority;
    private final String description;
}
