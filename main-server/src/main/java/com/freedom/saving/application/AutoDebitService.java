package com.freedom.saving.application;

import com.freedom.auth.domain.User;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.logging.Loggable;
import com.freedom.saving.domain.payment.SavingPaymentHistory;
import com.freedom.saving.domain.payment.SavingPaymentHistoryRepository;
import com.freedom.saving.domain.subscription.SavingSubscription;
import com.freedom.saving.domain.subscription.SubscriptionStatus;
import com.freedom.saving.infra.snapshot.SavingSubscriptionJpaRepository;
import com.freedom.wallet.application.SavingTransactionService;
import com.freedom.wallet.domain.WalletTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AutoDebitService {

    private final UserJpaRepository userRepo;
    private final SavingSubscriptionJpaRepository subscriptionRepo;
    private final SavingPaymentHistoryRepository paymentRepo;
    private final SavingTransactionService savingTxnService;
    private final PlatformTransactionManager txManager;

    /**
     * 인증된 사용자에 대해, 오늘 첫 호출 시 자동납입 수행.
     * - User.lastAutoPaymentDate 와 오늘 비교해 이미 처리되었으면 즉시 종료
     * - 처리 후 lastAutoPaymentDate = today 업데이트
     */
    @Loggable("자동 납입 처리")
    public void runOncePerDay(Long userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return;
        LocalDate today = LocalDate.now();
        if (today.equals(user.getLastAutoPaymentDate())) return;

        // 활성 구독들 조회
        List<SavingSubscription> actives = subscriptionRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);

        // 구독별로 독립 트랜잭션으로 처리 (부분 성공 허용)
        for (SavingSubscription sub : actives) {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("auto-debit-sub-" + sub.getId());
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus status = txManager.getTransaction(def);
            try {
                paymentRepo.findNextPlannedPayment(sub.getId()).ifPresent(planned -> {
                    BigDecimal amount = planned.getExpectedAmount();
                    if (amount == null || amount.signum() <= 0) return;
                    String requestId = "AUTO_" + UUID.randomUUID();
                    // 출금 및 거래 생성 (잔액 부족 시 도메인 예외 -> 상위 catch로 전파)
                    WalletTransaction txn = savingTxnService.processSavingAutoDebit(userId, requestId, amount, sub.getId());
                    // 납입 이력 반영
                    planned.markPaid(amount, txn.getId(), null);
                    paymentRepo.save(planned);
                });
                txManager.commit(status);
            } catch (Exception e) {
                // 실패 시 롤백 후 미납 처리 + 3회 누적 시 강제 해지
                txManager.rollback(status);

                DefaultTransactionDefinition missDef = new DefaultTransactionDefinition();
                missDef.setName("auto-debit-miss-" + sub.getId());
                missDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus missTx = txManager.getTransaction(missDef);
                try {
                    paymentRepo.findNextPlannedPayment(sub.getId()).ifPresent(planned -> {
                        planned.markMissed();
                        paymentRepo.save(planned);
                    });
                    long missed = paymentRepo.countBySubscriptionIdAndStatus(sub.getId(), SavingPaymentHistory.PaymentStatus.MISSED);
                    if (missed >= 3 && sub.getStatus() == SubscriptionStatus.ACTIVE) {
                        sub.forceCancel();
                        subscriptionRepo.save(sub);
                    }
                    txManager.commit(missTx);
                } catch (Exception ex) {
                    txManager.rollback(missTx);
                }
            }
        }

        // 모든 구독 처리 후, 최종적으로 오늘 처리 기록 업데이트 (별도 트랜잭션)
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("auto-debit-last-date");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = txManager.getTransaction(def);
        try {
            User u = userRepo.findById(userId).orElseThrow();
            u.updateLastAutoPaymentDate(today);
            userRepo.save(u);
            txManager.commit(status);
        } catch (Exception e) {
            txManager.rollback(status);
        }
    }
}
