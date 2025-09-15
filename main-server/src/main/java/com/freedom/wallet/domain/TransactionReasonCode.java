package com.freedom.wallet.domain;

/**
 * 거래 사유 코드 열거형
 */
public enum TransactionReasonCode {
    // 퀴즈 관련
    QUIZ_REWARD,         // 퀴즈 보상
    
    // 출석 관련
    ATTENDANCE_REWARD,   // 출석 보상
    
    // 적금 관련
    SAVING_JOIN,         // 적금 가입
    SAVING_CANCEL,       // 적금 해제
    SAVING_MATURITY,     // 적금 만기
    SAVING_INTEREST,     // 적금 이자
    SAVING_AUTO_DEBIT,   // 적금 자동 납입
    SAVING_MANUAL_PAYMENT // 적금 수동 납입
}
