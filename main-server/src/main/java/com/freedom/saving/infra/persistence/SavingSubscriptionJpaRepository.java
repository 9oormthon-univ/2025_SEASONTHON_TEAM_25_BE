package com.freedom.saving.infra.persistence;

import com.freedom.saving.domain.model.entity.SavingSubscription;
import com.freedom.saving.domain.model.vo.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingSubscriptionJpaRepository extends JpaRepository<SavingSubscription, Long> {

    List<SavingSubscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    List<SavingSubscription> findByUserIdAndStatusIn(Long userId, List<SubscriptionStatus> statuses);

    Optional<SavingSubscription> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndProductSnapshotIdAndStatus(Long userId, Long productSnapshotId, SubscriptionStatus status);
}
