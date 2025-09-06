package com.freedom.saving.infra.payment;

import com.freedom.saving.domain.payment.SavingPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SavingPaymentHistoryJpaRepository extends JpaRepository<SavingPaymentHistory, Long> {

    @Query("select coalesce(sum(p.paidAmount), 0) from SavingPaymentHistory p where p.subscriptionId = :subscriptionId and p.status in ('PAID','PARTIAL')")
    BigDecimal calculateTotalPaidAmount(@Param("subscriptionId") Long subscriptionId);

    long countBySubscriptionIdAndStatus(Long subscriptionId, SavingPaymentHistory.PaymentStatus status);

    @Query("select p from SavingPaymentHistory p where p.subscriptionId = :subscriptionId and p.status = 'PLANNED' order by p.dueServiceDate asc")
    Optional<SavingPaymentHistory> findNextPlannedPayment(@Param("subscriptionId") Long subscriptionId);

    Optional<SavingPaymentHistory> findBySubscriptionIdAndCycleNo(Long subscriptionId, Integer cycleNo);

    Optional<SavingPaymentHistory> findFirstBySubscriptionIdAndStatusAndDueServiceDateGreaterThanEqualOrderByDueServiceDateAsc(
            @Param("subscriptionId") Long subscriptionId,
            @Param("status") SavingPaymentHistory.PaymentStatus status,
            @Param("fromDate") LocalDate fromDate);
}
