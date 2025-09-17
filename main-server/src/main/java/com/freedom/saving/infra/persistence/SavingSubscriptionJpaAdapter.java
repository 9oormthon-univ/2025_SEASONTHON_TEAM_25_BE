package com.freedom.saving.infra.persistence;

import com.freedom.saving.application.port.SavingSubscriptionPort;
import com.freedom.saving.domain.model.vo.AutoDebitAmount;
import com.freedom.saving.domain.model.entity.SavingSubscription;
import com.freedom.saving.domain.model.vo.ServiceDates;
import com.freedom.saving.domain.model.vo.TermMonths;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.freedom.saving.domain.SubscriptionStatus;
import com.freedom.common.exception.custom.SavingExceptions;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SavingSubscriptionPort 구현체 (Infra -> JPA)
 * - Application은 본 Port만 의존하고, 구현 세부(JPA)는 infra에 숨김
 * - 생성/저장 책임만 가짐. 규칙/검증은 Application Service에서 수행
 */
@Component
@RequiredArgsConstructor
public class SavingSubscriptionJpaAdapter implements SavingSubscriptionPort {

    private final SavingSubscriptionJpaRepository repository; // ← 패키지 경로 확인: infra.subscription

    @Override
    @Transactional
    public Long open(Long userId,
                     Long productSnapshotId,
                     int termMonths,
                     String reserveTypeCode,      // 현재 도메인에 저장 필드 없으면 미사용
                     BigDecimal autoDebitAmount,  // 정액식(S)일 땐 Service에서 필수 검증
                     LocalDate startServiceDate,
                     LocalDate maturityServiceDate) {

        // 0) 중복 가입 방지: 동일 사용자/상품에 ACTIVE가 존재하면 예외
        if (repository.existsByUserIdAndProductSnapshotIdAndStatus(userId, productSnapshotId, SubscriptionStatus.ACTIVE)) {
            throw new SavingExceptions.SavingDuplicateSubscriptionException();
        }

        // 1) VO 생성(기본 제약은 VO가 검증)
        TermMonths term = new TermMonths(termMonths);
        AutoDebitAmount amount = new AutoDebitAmount(autoDebitAmount);
        ServiceDates dates = new ServiceDates(startServiceDate, maturityServiceDate);

        // 2) 애그리거트 생성
        SavingSubscription entity = SavingSubscription.open(
                userId,
                productSnapshotId,
                amount,
                term,
                dates
        );

        // 3) 저장 후 식별자 반환
        return repository.save(entity).getId();
    }
}
