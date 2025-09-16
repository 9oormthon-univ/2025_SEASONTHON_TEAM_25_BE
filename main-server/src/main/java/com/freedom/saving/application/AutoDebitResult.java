package com.freedom.saving.application;

/**
 * 자동납입 처리 결과
 */
public enum AutoDebitResult {
    SUCCESS,    // 성공
    FAILURE,    // 실패
    SKIPPED     // 스킵 (처리할 것이 없음)
}
