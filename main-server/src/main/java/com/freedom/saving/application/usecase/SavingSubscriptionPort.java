package com.freedom.saving.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * 구독(가입) 생성 저장 포트(Out Port)
 * - 도메인 애그리거트 생성/저장은 infra 구현체에서 담당하도록 추상화
 * - 애플리케이션 서비스는 검증/정책만 수행 후 본 포트로 위임
 */
public interface SavingSubscriptionPort {

    Long open(Long userId, Long productSnapshotId,
              int termMonths, String reserveTypeCode,
              BigDecimal autoDebitAmount,
              LocalDate startServiceDate, LocalDate maturityServiceDate);
}
