package com.freedom.saving.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface SavingPaymentHistoryRepository {

    SavingPaymentHistory save(SavingPaymentHistory entity);

    BigDecimal calculateTotalPaidAmount(Long subscriptionId);

    long countBySubscriptionIdAndStatus(Long subscriptionId, SavingPaymentHistory.PaymentStatus status);

    Optional<SavingPaymentHistory> findNextPlannedPayment(Long subscriptionId);

    Optional<SavingPaymentHistory> findBySubscriptionIdAndCycleNo(Long subscriptionId, Integer cycleNo);

    /**
     * fromDate(이상) 기준으로 다음 PLANNED 한 건 조회
     */
    Optional<SavingPaymentHistory> findNextPlannedPaymentFromDate(Long subscriptionId, LocalDate fromDate);
}
