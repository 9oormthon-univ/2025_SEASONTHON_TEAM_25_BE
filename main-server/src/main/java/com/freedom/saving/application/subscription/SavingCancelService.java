package com.freedom.saving.application.subscription;

import com.freedom.common.logging.Loggable;
import com.freedom.saving.domain.model.entity.SavingSubscription;
import com.freedom.saving.domain.SubscriptionStatus;
import com.freedom.saving.infra.persistence.SavingSubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.freedom.common.exception.custom.SavingExceptions.*;

@Service
@RequiredArgsConstructor
public class SavingCancelService {

    private final SavingSubscriptionJpaRepository subscriptionRepo;

    @Loggable("적금 해지")
    @Transactional
    public void cancelByUser(Long userId, Long subscriptionId) {
        SavingSubscription sub = subscriptionRepo.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(SavingSubscriptionNotFoundException::new);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new SavingSubscriptionInvalidStateException(sub.getStatus().name());
        }
        sub.cancelByUser();
        subscriptionRepo.save(sub);
    }
}
