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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 개편된 자동납입 서비스
 * 
 * 주요 개선사항:
 * 1. 접속한 사용자만 하루에 1회 자동납입
 * 2. 단순화된 트랜잭션 관리 (@Transactional 활용)
 * 3. 명확한 날짜 검증 (오늘 납입 예정인지 확인)
 * 4. 개선된 에러 처리 및 로깅
 * 5. 성능 최적화 (조건부 실행)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoDebitService {

    private final UserJpaRepository userRepo;
    private final SavingSubscriptionJpaRepository subscriptionRepo;
    private final SavingPaymentHistoryRepository paymentRepo;
    private final SavingTransactionService savingTxnService;

    /**
     * 접속한 사용자에 대해 하루에 1회 자동 납입 수행
     * 
     * 동작 방식:
     * 1. 사용자 존재 여부 확인
     * 2. 오늘 이미 처리되었는지 확인 (lastAutoPaymentDate 체크)
     * 3. 활성 구독 조회
     * 4. 각 구독별로 오늘 납입 예정인지 확인 후 처리
     * 5. 처리 완료 후 lastAutoPaymentDate 업데이트
     * 
     * @param userId 사용자 ID
     */
    @Loggable("자동납입 처리")
    public void runOncePerDay(Long userId) {
        log.info("자동납입 시작 - 사용자 ID: {}", userId);
        
        // 1. 사용자 존재 여부 확인
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            log.warn("사용자를 찾을 수 없음 - ID: {}", userId);
            return;
        }
        
        // 2. 오늘 이미 처리되었는지 확인
        LocalDate today = LocalDate.now();
        if (today.equals(user.getLastAutoPaymentDate())) {
            log.info("이미 오늘 자동납입 처리 완료 - 사용자 ID: {}", userId);
            return;
        }
        
        // 3. 활성 구독 조회
        List<SavingSubscription> activeSubscriptions = subscriptionRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        log.info("활성 구독 수: {} - 사용자 ID: {}", activeSubscriptions.size(), userId);
        
        if (activeSubscriptions.isEmpty()) {
            log.info("활성 구독이 없음 - 사용자 ID: {}", userId);
            updateLastAutoPaymentDate(user, today);
            return;
        }
        
        // 4. 각 구독별로 자동납입 처리
        int successCount = 0;
        int failureCount = 0;
        
        for (SavingSubscription subscription : activeSubscriptions) {
            try {
                boolean processed = processSubscriptionAutoDebit(userId, subscription, today);
                if (processed) {
                    successCount++;
                }
            } catch (Exception e) {
                failureCount++;
                log.error("구독 자동납입 실패 - 구독 ID: {}, 사용자 ID: {}, 오류: {}", 
                         subscription.getId(), userId, e.getMessage(), e);
            }
        }
        
        // 5. 처리 완료 후 마지막 처리 날짜 업데이트
        updateLastAutoPaymentDate(user, today);
        
        log.info("자동납입 완료 - 사용자 ID: {}, 성공: {}, 실패: {}", userId, successCount, failureCount);
    }
    
    /**
     * 개별 구독에 대한 자동납입 처리
     * 
     * @param userId 사용자 ID
     * @param subscription 구독 정보
     * @param today 오늘 날짜
     * @return 처리 여부 (true: 처리됨, false: 처리할 것이 없음)
     */
    @Transactional
    public boolean processSubscriptionAutoDebit(Long userId, SavingSubscription subscription, LocalDate today) {
        log.debug("구독 자동납입 처리 시작 - 구독 ID: {}, 사용자 ID: {}", subscription.getId(), userId);
        
        // 1. 다음 납입 계획 조회
        SavingPaymentHistory plannedPayment = paymentRepo.findNextPlannedPayment(subscription.getId())
                .orElse(null);
        
        if (plannedPayment == null) {
            log.debug("다음 납입 계획이 없음 - 구독 ID: {}", subscription.getId());
            return false;
        }
        
        // 2. 오늘 납입 예정인지 확인
        if (!today.equals(plannedPayment.getDueServiceDate())) {
            log.debug("오늘 납입 예정이 아님 - 구독 ID: {}, 예정일: {}, 오늘: {}", 
                     subscription.getId(), plannedPayment.getDueServiceDate(), today);
            return false;
        }
        
        // 3. 납입 금액 유효성 확인
        BigDecimal amount = plannedPayment.getExpectedAmount();
        if (amount == null || amount.signum() <= 0) {
            log.warn("유효하지 않은 납입 금액 - 구독 ID: {}, 금액: {}", subscription.getId(), amount);
            return false;
        }
        
        // 4. 자동 납입 실행
        try {
            String requestId = "AUTO_" + UUID.randomUUID();
            log.info("자동납입 실행 - 구독 ID: {}, 금액: {}, 요청 ID: {}", 
                    subscription.getId(), amount, requestId);
            
            WalletTransaction transaction = savingTxnService.processSavingAutoDebit(
                    userId, requestId, amount, subscription.getId());
            
            // 5. 납입 이력 업데이트
            plannedPayment.markPaid(amount, transaction.getId(), null);
            paymentRepo.save(plannedPayment);
            
            log.info("자동납입 성공 - 구독 ID: {}, 금액: {}, 거래 ID: {}", 
                    subscription.getId(), amount, transaction.getId());
            
            return true;
            
        } catch (Exception e) {
            log.error("자동납입 실패 - 구독 ID: {}, 금액: {}, 오류: {}", 
                     subscription.getId(), amount, e.getMessage(), e);
            
            // 6. 실패 시 미납 처리
            handlePaymentFailure(subscription, plannedPayment);
            
            return false;
        }
    }
    
    /**
     * 납입 실패 시 미납 처리 및 강제 해지 검토
     * 
     * @param subscription 구독 정보
     * @param plannedPayment 납입 계획
     */
    @Transactional
    public void handlePaymentFailure(SavingSubscription subscription, SavingPaymentHistory plannedPayment) {
        log.info("납입 실패 처리 시작 - 구독 ID: {}", subscription.getId());
        
        try {
            // 1. 미납으로 마킹
            plannedPayment.markMissed();
            paymentRepo.save(plannedPayment);
            
            // 2. 미납 횟수 확인
            long missedCount = paymentRepo.countBySubscriptionIdAndStatus(
                    subscription.getId(), SavingPaymentHistory.PaymentStatus.MISSED);
            
            log.info("미납 횟수: {} - 구독 ID: {}", missedCount, subscription.getId());
            
            // 3. 3회 이상 미납 시 강제 해지
            if (missedCount >= 3 && subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                log.warn("미납 3회 누적으로 강제 해지 - 구독 ID: {}", subscription.getId());
                subscription.forceCancel();
                subscriptionRepo.save(subscription);
            }
            
        } catch (Exception e) {
            log.error("납입 실패 처리 중 오류 발생 - 구독 ID: {}, 오류: {}", 
                     subscription.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 사용자의 마지막 자동납입 처리 날짜 업데이트
     * 
     * @param user 사용자 정보
     * @param today 오늘 날짜
     */
    @Transactional
    public void updateLastAutoPaymentDate(User user, LocalDate today) {
        try {
            user.updateLastAutoPaymentDate(today);
            userRepo.save(user);
            log.debug("마지막 자동납입 처리 날짜 업데이트 완료 - 사용자 ID: {}, 날짜: {}", 
                     user.getId(), today);
        } catch (Exception e) {
            log.error("마지막 자동납입 처리 날짜 업데이트 실패 - 사용자 ID: {}, 오류: {}", 
                     user.getId(), e.getMessage(), e);
        }
    }
}
