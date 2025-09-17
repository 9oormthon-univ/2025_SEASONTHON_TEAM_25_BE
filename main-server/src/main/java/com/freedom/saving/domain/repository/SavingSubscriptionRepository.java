package com.freedom.saving.domain.repository;

import com.freedom.saving.domain.entities.SavingSubscription;

import java.util.Optional;

/**
 * 도메인 레벨 추상화
 * infra(JPA) 구현체는 saving/infra 하위에 별도 작성
 */
public interface SavingSubscriptionRepository {

    SavingSubscription save(SavingSubscription subscription);

    Optional<SavingSubscription> findById(Long id);

    /**
     * 동일 사용자/상품에 대해 ACTIVE 상태 이미 있는지 체크
     * 중복 가입 방지
     */
    boolean existsActiveByUserIdAndProductSnapshotId(Long userId, Long productSnapshotId);
}
