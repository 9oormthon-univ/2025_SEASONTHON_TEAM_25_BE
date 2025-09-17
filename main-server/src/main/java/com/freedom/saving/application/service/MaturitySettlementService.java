package com.freedom.saving.application.service;

import com.freedom.common.time.TimeProvider;
import com.freedom.saving.domain.repository.SavingPaymentHistoryRepository;
import com.freedom.saving.domain.model.entity.SavingPaymentHistory;
import com.freedom.saving.domain.model.entity.SavingSubscription;
import com.freedom.saving.domain.SubscriptionStatus;
import com.freedom.saving.infra.SavingProductOptionSnapshotJpaRepository;
import com.freedom.saving.infra.SavingSubscriptionJpaRepository;
import com.freedom.wallet.application.SavingTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

import static com.freedom.common.exception.custom.SavingExceptions.*;

@Service
@RequiredArgsConstructor
public class MaturitySettlementService {

    private final SavingSubscriptionJpaRepository subscriptionRepo;
    private final SavingPaymentHistoryRepository paymentRepo;
    private final SavingProductOptionSnapshotJpaRepository optionRepo;
    private final SavingTransactionService savingTxnService;
    private final TimeProvider timeProvider;

    public record PayoutQuote(BigDecimal principal, BigDecimal rate, BigDecimal interest, BigDecimal total) {}



    /**
     * 만기 정산 처리: 납입 총액 + 이자(intr_rate 적용)를 지갑에 입금하고, 구독 상태를 MATURED로 전환
     */
    @Transactional
    public PayoutQuote settleMaturity(Long userId, Long subscriptionId) {
        SavingSubscription sub = subscriptionRepo.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(SavingSubscriptionNotFoundException::new);
        // 만기일 경과 여부 확인
        ensureMaturedByDate(sub);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new SavingSubscriptionInvalidStateException(sub.getStatus().name());
        }

        PayoutQuote quote = computeQuote(sub);

        // 멱등키 생성 후 만기 입금 처리
        String requestId = "MAT_" + UUID.randomUUID();
        savingTxnService.processSavingMaturity(userId, requestId, quote.total(), subscriptionId);

        // 구독 상태 변경
        sub.mature();
        subscriptionRepo.save(sub);
        return quote;
    }


    private void ensureMaturedByDate(SavingSubscription sub) {
        LocalDate today = timeProvider.today();
        if (today.isBefore(sub.getDates().getMaturityDate())) {
            throw new SavingSubscriptionInvalidStateException("NOT_MATURED_YET");
        }
    }

    /**
     * 미납 1~2회 시 이자는 (유효 회차/총 회차) 비율만큼 지급한다.
     */
    private PayoutQuote computeQuote(SavingSubscription sub) {
        Long subscriptionId = sub.getId();
        BigDecimal principal = paymentRepo.calculateTotalPaidAmount(subscriptionId);
        var opt = optionRepo.findFirstByProductSnapshotIdAndSaveTrmMonthsOrderByIntrRateDesc(
                sub.getProductSnapshotId(), sub.getTerm().getValue());
        BigDecimal rate = opt != null ? opt.getIntrRate() : BigDecimal.ZERO;

        int totalTicks = Math.max(1, sub.getTerm().getValue());
        long missed = paymentRepo.countBySubscriptionIdAndStatus(subscriptionId, SavingPaymentHistory.PaymentStatus.MISSED);
        int effectiveTicks = Math.max(0, totalTicks - (int) missed);

        BigDecimal factor = BigDecimal.valueOf(effectiveTicks)
                .divide(BigDecimal.valueOf(totalTicks), 4, RoundingMode.DOWN);

        BigDecimal interest = principal
                .multiply(rate)
                .multiply(factor)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        BigDecimal total = principal.add(interest);
        return new PayoutQuote(principal, rate, interest, total);
    }
}
