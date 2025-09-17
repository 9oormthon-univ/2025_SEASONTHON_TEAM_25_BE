package com.freedom.saving.infra.persistence;

import com.freedom.saving.domain.model.entity.SavingPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static com.freedom.saving.domain.model.entity.SavingPaymentHistory.*;

@Repository
public interface SavingPaymentHistoryJpaRepository extends JpaRepository<SavingPaymentHistory, Long> {

    @Query("select coalesce(sum(p.paidAmount), 0) from SavingPaymentHistory p where p.subscriptionId = :subscriptionId and p.status in ('PAID','PARTIAL')")
    BigDecimal calculateTotalPaidAmount(@Param("subscriptionId") Long subscriptionId);

    long countBySubscriptionIdAndStatus(Long subscriptionId, PaymentStatus status);

    Optional<SavingPaymentHistory> findFirstBySubscriptionIdAndStatusOrderByDueServiceDateAsc(Long subscriptionId, PaymentStatus status);

    Optional<SavingPaymentHistory> findBySubscriptionIdAndCycleNo(Long subscriptionId, Integer cycleNo);

    Optional<SavingPaymentHistory> findFirstBySubscriptionIdAndStatusAndDueServiceDateGreaterThanEqualOrderByDueServiceDateAsc(
            @Param("subscriptionId") Long subscriptionId,
            @Param("status") PaymentStatus status,
            @Param("fromDate") LocalDate fromDate);
}
