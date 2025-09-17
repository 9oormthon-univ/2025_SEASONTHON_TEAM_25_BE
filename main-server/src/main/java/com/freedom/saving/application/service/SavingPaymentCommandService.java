package com.freedom.saving.application.service;

import com.freedom.common.logging.Loggable;
import com.freedom.saving.domain.model.entity.SavingPaymentHistory;
import com.freedom.saving.domain.repository.SavingPaymentHistoryRepository;
import com.freedom.saving.domain.model.entity.SavingSubscription;
import com.freedom.saving.domain.model.vo.SubscriptionStatus;
import com.freedom.saving.infra.persistence.SavingSubscriptionJpaRepository;
import com.freedom.common.time.TimeProvider;
import com.freedom.wallet.application.SavingTransactionService;
import com.freedom.saving.domain.policy.TickPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.freedom.common.exception.custom.SavingExceptions.*;

@Service
@RequiredArgsConstructor
public class SavingPaymentCommandService {

    private final SavingSubscriptionJpaRepository subscriptionRepo;
    private final SavingPaymentHistoryRepository paymentRepo;
    private final SavingTransactionService savingTxnService;
    private final TimeProvider timeProvider;
    private final TickPolicy tickPolicy;

    /**
     * 다음 예정 회차(PLANNED)에 대해 납입 처리
     * amount가 null이면 expectedAmount로 처리
     */
    @Loggable("적금 납입 처리")
    @Transactional
    public void depositNext(Long userId, Long subscriptionId, BigDecimal amount) {
        SavingSubscription sub = subscriptionRepo.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(SavingSubscriptionNotFoundException::new);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new SavingSubscriptionInvalidStateException(sub.getStatus().name());
        }

        // 스케줄이 없던 과거 가입분을 위한 자동 보정: PLANNED 없으면 생성 후 '오늘 기준' 재조회
        LocalDate today = timeProvider.today();
        SavingPaymentHistory planned = paymentRepo.findNextPlannedPaymentFromDate(subscriptionId, today)
                .orElseGet(() -> {
                    backfillPlannedScheduleIfMissing(sub);
                    return paymentRepo.findNextPlannedPaymentFromDate(subscriptionId, today).orElse(null);
                });
        if (planned == null) {
            throw new SavingNoNextPlannedPaymentException();
        }

        BigDecimal payAmount = amount != null ? amount : planned.getExpectedAmount();
        if (payAmount == null || payAmount.signum() <= 0) {
            throw new SavingInvalidPaymentAmountException();
        }

        // 하루 1회 제한: 오늘 회차만 허용
        if (!today.equals(planned.getDueServiceDate())) {
            throw new SavingPolicyInvalidException("오늘 납입 가능한 회차가 없습니다.");
        }

        // 지갑 출금 + 멱등 처리 (requestId = MANUAL_subId_yyyy-MM-dd)
        String requestId = "MANUAL_" + subscriptionId + "_" + today.toString();
        var txn = savingTxnService.processSavingManualPayment(userId, requestId, payAmount, subscriptionId);


        planned.markPaid(payAmount, txn.getId(), null);
        paymentRepo.save(planned);
    }

    /**
     * 과거 가입 데이터에 납입 스케줄(PLANNED)이 전혀 없는 경우 전체 스케줄을 생성해 보정한다.
     */
    private void backfillPlannedScheduleIfMissing(SavingSubscription sub) {
        // 안전장치: 금액/기간/시작일 유효성 확인
        if (sub.getAutoDebitAmount() == null || sub.getAutoDebitAmount().getValue() == null) return;
        if (sub.getTerm() == null || sub.getTerm().getValue() == null || sub.getTerm().getValue() <= 0) return;
        if (sub.getDates() == null || sub.getDates().getStartDate() == null) return;

        LocalDate start = sub.getDates().getStartDate();
        LocalDate firstDue = tickPolicy.calcFirstTransferDate(start);
        BigDecimal expected = sub.getAutoDebitAmount().getValue();
        int term = sub.getTerm().getValue();

        for (int i = 1; i <= term; i++) {
            LocalDate due = firstDue.plusDays(i - 1L);
            SavingPaymentHistory h = SavingPaymentHistory.planned(sub.getId(), i, due, expected);
            paymentRepo.save(h);
        }
    }
}
