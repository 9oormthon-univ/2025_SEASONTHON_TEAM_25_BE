package com.freedom.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    
    ACTIVE("활성", "정상적으로 서비스를 이용할 수 있는 상태"),
    WITHDRAWN("탈퇴", "서비스에서 탈퇴한 상태"),
    SUSPENDED("정지", "관리자에 의해 일시적으로 서비스 이용이 제한된 상태");
    
    private final String displayName;
    private final String description;
}
