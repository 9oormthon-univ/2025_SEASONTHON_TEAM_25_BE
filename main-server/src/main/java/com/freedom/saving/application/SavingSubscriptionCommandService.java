package com.freedom.saving.application;

import com.freedom.common.exception.custom.SavingExceptions;
import com.freedom.common.logging.Loggable;
import com.freedom.saving.domain.subscription.SavingSubscription;
import com.freedom.saving.domain.subscription.SubscriptionStatus;
import com.freedom.saving.infra.snapshot.SavingSubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavingSubscriptionCommandService {

    private final SavingSubscriptionJpaRepository subscriptionRepo;

    @Loggable("적금 해지")
    @Transactional
    public void cancelByUser(Long userId, Long subscriptionId) {
        SavingSubscription sub = subscriptionRepo.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(SavingExceptions.SavingSubscriptionNotFoundException::new);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new SavingExceptions.SavingSubscriptionInvalidStateException(sub.getStatus().name());
        }
        sub.cancelByUser();
        subscriptionRepo.save(sub);
    }
}
