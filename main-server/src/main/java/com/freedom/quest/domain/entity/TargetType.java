package com.freedom.quest.domain.entity;

public enum TargetType {
    ATTENDANCE,        // 출석(yyyyMMdd 단위로 1일 1회 인정)
    NEWS,              // 뉴스 상세 열람
    QUIZ_CORRECT_ONLY, // 퀴즈 '정답'만 카운트 (중복 퀴즈 불가)
    SCRAP_NEWS,        // 뉴스 스크랩 ON
    SCRAP_QUIZ,        // 퀴즈 스크랩 ON
    FINANCE_PRODUCT    // 금융상품 가입 (존재 여부 1회)
}
