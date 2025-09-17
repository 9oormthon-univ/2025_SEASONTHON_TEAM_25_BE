package com.freedom.saving.domain;

public enum SubscriptionStatus {
    ACTIVE,           // 정상 진행
    MATURED,          // 만기 완료
    CANCELLED,        // 사용자 자발적 해지
    FORCED_CANCELLED  // 정책에 의한 강제 해지
}
