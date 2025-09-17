package com.freedom.saving.application.service;

import com.freedom.common.time.TimeProvider;
import com.freedom.saving.domain.model.entity.SavingPaymentHistory;
import com.freedom.saving.domain.repository.SavingPaymentHistoryRepository;
import com.freedom.saving.domain.model.entity.SavingSubscription;
import com.freedom.saving.domain.SubscriptionStatus;
import com.freedom.saving.domain.policy.TickPolicy;
import com.freedom.saving.infra.SavingProductSnapshotJpaRepository;
import com.freedom.saving.infra.SavingSubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavingSubscriptionQueryService {

    private final SavingSubscriptionJpaRepository subscriptionRepo;
    private final SavingProductSnapshotJpaRepository productSnapshotRepo;
    private final SavingPaymentHistoryRepository paymentRepo;
    private final TimeProvider timeProvider;
    private final TickPolicy tickPolicy;

    public record ActiveDto(Long subscriptionId, String productName, BigDecimal currentAmount,
                            int progressPercentage, String joinDate, String maturityDate, int remainingPayments) {}

    public record CompletedDto(Long subscriptionId, String productName, BigDecimal finalAmount,
                               String joinDate, String maturityDate, int progressPercentage) {}

    public List<ActiveDto> getActive(Long userId) {
        List<SavingSubscription> subs = subscriptionRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        return subs.stream().map(this::toActive).toList();
    }

    public List<CompletedDto> getCompleted(Long userId) {
        List<SavingSubscription> subs = subscriptionRepo.findByUserIdAndStatusIn(userId,
                List.of(SubscriptionStatus.MATURED));
        return subs.stream().map(this::toCompleted).toList();
    }

    private ActiveDto toActive(SavingSubscription s) {
        BigDecimal target = s.getAutoDebitAmount().getValue().multiply(BigDecimal.valueOf(s.getTerm().getValue()));
        BigDecimal paid = paymentRepo.calculateTotalPaidAmount(s.getId());

        int progress = target.signum() == 0 ? 0 : paid.multiply(BigDecimal.valueOf(100)).divide(target, 0, java.math.RoundingMode.HALF_UP).intValue();

        long paidOrPartial = paymentRepo.countBySubscriptionIdAndStatus(s.getId(), SavingPaymentHistory.PaymentStatus.PAID)
                + paymentRepo.countBySubscriptionIdAndStatus(s.getId(), SavingPaymentHistory.PaymentStatus.PARTIAL);

        int remaining = Math.max(0, s.getTerm().getValue() - (int) paidOrPartial);

        String productName = productSnapshotRepo.findById(s.getProductSnapshotId())
                .map(snapshot -> snapshot.getFinPrdtNm())
                .orElse("");

        LocalDate join = s.getDates().getStartDate();
        LocalDate maturityByPolicy = tickPolicy.calcMaturityDate(join, s.getTerm().getValue());

        return new ActiveDto(
                s.getId(),
                productName,
                paid,
                Math.min(100, progress),
                join.toString(),
                maturityByPolicy.toString(),
                remaining
        );
    }

    private CompletedDto toCompleted(SavingSubscription s) {
        BigDecimal finalAmount = paymentRepo.calculateTotalPaidAmount(s.getId());
        String productName = productSnapshotRepo.findById(s.getProductSnapshotId())
                .map(snapshot -> snapshot.getFinPrdtNm())
                .orElse("");

        LocalDate join = s.getDates().getStartDate();
        LocalDate maturityByPolicy = tickPolicy.calcMaturityDate(join, s.getTerm().getValue());

        return new CompletedDto(
                s.getId(),
                productName,
                finalAmount,
                join.toString(),
                maturityByPolicy.toString(),
                100
        );
    }
}
