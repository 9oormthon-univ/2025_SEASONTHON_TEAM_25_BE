package com.freedom.saving.application;

import com.freedom.common.exception.custom.SavingExceptions;
import com.freedom.common.time.TimeProvider;
import com.freedom.saving.domain.SavingProductOptionSnapshot;
import com.freedom.saving.domain.payment.SavingPaymentHistoryRepository;
import com.freedom.saving.domain.payment.SavingPaymentHistory;
import com.freedom.saving.domain.subscription.SavingSubscription;
import com.freedom.saving.domain.subscription.SubscriptionStatus;
import com.freedom.saving.infra.snapshot.SavingProductOptionSnapshotJpaRepository;
import com.freedom.saving.infra.snapshot.SavingProductSnapshotJpaRepository;
import com.freedom.saving.infra.snapshot.SavingSubscriptionJpaRepository;
import com.freedom.wallet.application.SavingTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaturitySettlementService {

    private final SavingSubscriptionJpaRepository subscriptionRepo;
    private final SavingPaymentHistoryRepository paymentRepo;
    private final SavingProductOptionSnapshotJpaRepository optionRepo;
    private final SavingProductSnapshotJpaRepository productSnapshotRepo;
    private final SavingTransactionService savingTxnService;
    private final TimeProvider timeProvider;

    public record PayoutQuote(BigDecimal principal, BigDecimal rate, BigDecimal interest, BigDecimal total) {}

    public record PendingMaturityDto(Long subscriptionId, String productName,
                                     BigDecimal principal, BigDecimal interest, BigDecimal total,
                                     String joinDate, String maturityDate) {}


    /**
     * 만기 정산 처리: 납입 총액 + 이자(intr_rate 적용)를 지갑에 입금하고, 구독 상태를 MATURED로 전환
     */
    @Transactional
    public PayoutQuote settleMaturity(Long userId, Long subscriptionId) {
        SavingSubscription sub = subscriptionRepo.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(SavingExceptions.SavingSubscriptionNotFoundException::new);
        // 만기일 경과 여부 확인
        ensureMaturedByDate(sub);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new SavingExceptions.SavingSubscriptionInvalidStateException(sub.getStatus().name());
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

    /**
     * 오늘 기준 만기일이 지난 ACTIVE 구독들의 모달 표시용 정보
     */
    @Transactional(readOnly = true)
    public List<PendingMaturityDto> listPendingMaturities(Long userId) {
        LocalDate today = timeProvider.today();
        List<SavingSubscription> actives = subscriptionRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        return actives.stream()
                .filter(sub -> !today.isBefore(sub.getDates().getMaturityDate()))
                .map(sub -> {
                    PayoutQuote q = computeQuote(sub);
                    String productName = productSnapshotRepo.findById(sub.getProductSnapshotId())
                            .map(s -> s.getFinPrdtNm()).orElse("");
                    return new PendingMaturityDto(
                            sub.getId(),
                            productName,
                            q.principal(), q.interest(), q.total(),
                            sub.getDates().getStartDate().toString(),
                            sub.getDates().getMaturityDate().toString()
                    );
                })
                .toList();
    }

    private void ensureMaturedByDate(SavingSubscription sub) {
        LocalDate today = timeProvider.today();
        if (today.isBefore(sub.getDates().getMaturityDate())) {
            throw new SavingExceptions.SavingSubscriptionInvalidStateException("NOT_MATURED_YET");
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
