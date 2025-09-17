package com.freedom.saving.application;

import com.freedom.auth.domain.User;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.saving.application.payment.AutoDebitService;
import com.freedom.saving.domain.model.entity.SavingPaymentHistory;
import com.freedom.saving.domain.model.vo.ServiceDates;
import com.freedom.saving.domain.SubscriptionStatus;
import com.freedom.saving.domain.repository.SavingPaymentHistoryRepository;
import com.freedom.saving.domain.model.vo.AutoDebitAmount;
import com.freedom.saving.domain.model.entity.SavingSubscription;
import com.freedom.saving.domain.model.vo.TermMonths;
import com.freedom.saving.infra.persistence.SavingSubscriptionJpaRepository;
import com.freedom.wallet.application.SavingTransactionService;
import com.freedom.wallet.domain.UserWallet;
import com.freedom.wallet.domain.WalletTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat; // AssertJ
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 테스트 포인트
 * assertThat으로 "저장되는 값"을 검증
 * ArgumentCaptor로 repo.save(...)에 전달된 엔티티 값을 캡처해 상태 검증
 */
@ExtendWith(MockitoExtension.class)
class AutoDebitServiceTest {

    @Mock private UserJpaRepository userRepo;
    @Mock private SavingSubscriptionJpaRepository subscriptionRepo;
    @Mock private SavingPaymentHistoryRepository paymentRepo;
    @Mock private SavingTransactionService savingTxnService;

    @InjectMocks
    private AutoDebitService autoDebitService;

    // 저장 시점의 엔티티 상태를 검증하기 위해 사용
    @Captor private ArgumentCaptor<SavingPaymentHistory> paymentCaptor;
    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<SavingSubscription> subscriptionCaptor;

    private User testUser;
    private SavingSubscription testSubscription;
    private SavingPaymentHistory testPaymentHistory;
    private WalletTransaction testWalletTransaction;

    private final LocalDate today = LocalDate.now(); // 테스트 실행 날짜 고정(가독성/일관성)

    @BeforeEach
    void setUp() {
        // Given: 테스트 기본 데이터
        testUser = User.builder()
                .email("test@example.com")
                .password("password")
                .characterName("테스트캐릭터")
                .build();

        testSubscription = SavingSubscription.open(
                1L, // userId
                1L, // productSnapshotId
                new AutoDebitAmount(new BigDecimal("100000")),
                new TermMonths(12),
                new ServiceDates(
                        today.minusDays(1), // startDate
                        today.plusDays(11)  // maturityDate
                )
        );

        // 리플렉션으로 ID 세팅 (테스트 용도: 도메인 규칙엔 영향 없음)
        try {
            Field idField = SavingSubscription.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testSubscription, 1L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set subscription ID", e);
        }

        testPaymentHistory = SavingPaymentHistory.planned(
                1L, // subscriptionId
                1,  // cycleNo
                today, // dueServiceDate: 오늘
                new BigDecimal("100000") // expectedAmount
        );

        UserWallet testWallet = UserWallet.create(1L);
        testWallet.deposit(new BigDecimal("1000000")); // 넉넉한 잔액

        testWalletTransaction = WalletTransaction.createSavingAutoDebit(
                testWallet,
                "AUTO_test123",
                new BigDecimal("100000"),
                1L
        );
    }

    @Test
    @DisplayName("자동납입 성공 - 정상적인 자동납입이 처리되어야 한다")
    void runOncePerDay_Success() {
        // Given
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepo.findByUserIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(testSubscription));
        when(paymentRepo.findNextPlannedPayment(1L))
                .thenReturn(Optional.of(testPaymentHistory));
        when(savingTxnService.processSavingAutoDebit(anyLong(), anyString(), any(BigDecimal.class), anyLong()))
                .thenReturn(testWalletTransaction);

        // When
        autoDebitService.runOncePerDay(1L);

        // Then
        // 1) 저장된 납입 히스토리의 상태/금액이 기대대로인지 검증
        verify(paymentRepo, times(1)).save(paymentCaptor.capture());
        SavingPaymentHistory savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment).isNotNull();

        // 성공 시 상태가 PAID로 바뀌고, 납입 금액이 expectedAmount와 동일해야 함
        assertThat(savedPayment.getStatus())
                .as("자동납입 성공 시 PaymentStatus는 PAID 여야 한다")
                .isEqualTo(SavingPaymentHistory.PaymentStatus.PAID);
        assertThat(savedPayment.getPaidAmount())
                .as("납입 금액은 계획 금액과 동일해야 한다")
                .isEqualByComparingTo(new BigDecimal("100000"));

        // 2) 부가적 행위 검증(필요 최소한)
        verify(savingTxnService, times(1))
                .processSavingAutoDebit(eq(1L), anyString(), eq(new BigDecimal("100000")), eq(1L));
    }

    @Test
    @DisplayName("잔액 부족으로 인한 자동납입 실패 - 미납 처리되어야 한다")
    void runOncePerDay_InsufficientBalance_Failure() {
        // Given
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepo.findByUserIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(testSubscription));
        when(paymentRepo.findNextPlannedPayment(1L))
                .thenReturn(Optional.of(testPaymentHistory));
        when(savingTxnService.processSavingAutoDebit(anyLong(), anyString(), any(BigDecimal.class), anyLong()))
                .thenThrow(new RuntimeException("잔액이 부족합니다"));
        when(paymentRepo.countBySubscriptionIdAndStatus(1L, SavingPaymentHistory.PaymentStatus.MISSED))
                .thenReturn(1L); // 미납 1회

        // When
        autoDebitService.runOncePerDay(1L);

        // Then
        // 1) 미납으로 저장되었는지 확인
        verify(paymentRepo, atLeastOnce()).save(paymentCaptor.capture());
        SavingPaymentHistory missedPayment = paymentCaptor.getValue();
        assertThat(missedPayment.getStatus())
                .as("잔액 부족 시 PaymentStatus는 MISSED 여야 한다")
                .isEqualTo(SavingPaymentHistory.PaymentStatus.MISSED);

        // 2) 미납 카운트 조회가 수행되었는지 확인(강제해지 판단 로직)
        verify(paymentRepo, times(1))
                .countBySubscriptionIdAndStatus(1L, SavingPaymentHistory.PaymentStatus.MISSED);
    }


    @Test
    @DisplayName("활성 구독이 없는 경우 - 아무것도 처리되지 않아야 한다")
    void runOncePerDay_NoActiveSubscriptions_DoNothing() {
        // Given
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepo.findByUserIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(List.of());

        // When
        autoDebitService.runOncePerDay(1L);

        // Then
        // 1) 결제/히스토리 저장 호출 없음
        verify(savingTxnService, never()).processSavingAutoDebit(anyLong(), anyString(), any(BigDecimal.class), anyLong());
        verify(paymentRepo, never()).save(any(SavingPaymentHistory.class));
    }

    @Test
    @DisplayName("다음 납입 계획이 없는 경우 - 아무것도 처리되지 않아야 한다")
    void runOncePerDay_NoNextPlannedPayment_DoNothing() {
        // Given
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepo.findByUserIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(testSubscription));
        when(paymentRepo.findNextPlannedPayment(1L))
                .thenReturn(Optional.empty());

        // When
        autoDebitService.runOncePerDay(1L);

        // Then
        // 1) 결제/히스토리 저장 호출 없음
        verify(savingTxnService, never()).processSavingAutoDebit(anyLong(), anyString(), any(BigDecimal.class), anyLong());
        verify(paymentRepo, never()).save(any(SavingPaymentHistory.class));
    }

    @Test
    @DisplayName("미납 3회 누적 시 강제 해지되어야 한다")
    void runOncePerDay_MissedThreeTimes_ForceCancel() {
        // Given
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepo.findByUserIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(testSubscription));
        when(paymentRepo.findNextPlannedPayment(1L))
                .thenReturn(Optional.of(testPaymentHistory));
        when(savingTxnService.processSavingAutoDebit(anyLong(), anyString(), any(BigDecimal.class), anyLong()))
                .thenThrow(new RuntimeException("잔액이 부족합니다"));
        when(paymentRepo.countBySubscriptionIdAndStatus(1L, SavingPaymentHistory.PaymentStatus.MISSED))
                .thenReturn(3L); // 누적 3회

        // When
        autoDebitService.runOncePerDay(1L);

        // Then
        // 1) 미납 저장 검증
        verify(paymentRepo, atLeastOnce()).save(paymentCaptor.capture());
        SavingPaymentHistory missed = paymentCaptor.getValue();
        assertThat(missed.getStatus()).isEqualTo(SavingPaymentHistory.PaymentStatus.MISSED);

        // 2) 구독 강제 해지 저장 검증
        verify(subscriptionRepo, times(1)).save(subscriptionCaptor.capture());
        SavingSubscription canceled = subscriptionCaptor.getValue();
        assertThat(canceled.getStatus())
                .as("미납 3회 시 구독은 더 이상 ACTIVE가 아니어야 한다(강제 해지)")
                .isNotEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("여러 상품 가입 시 각각의 납입일 처리 - 오늘 이전 납입 예정인 상품들이 처리되어야 한다")
    void runOncePerDay_MultipleProducts_ProcessTodayAndPastDue() {
        // Given
        // 상품 1: 오늘 납입 예정
        SavingSubscription subscription1 = createTestSubscription(1L, 1L, today);
        SavingPaymentHistory payment1 = createTestPaymentHistory(1L, 1, today, new BigDecimal("100000"));
        
        // 상품 2: 내일 납입 예정 (스킵되어야 함 - 미래)
        SavingSubscription subscription2 = createTestSubscription(1L, 2L, today.plusDays(1));
        SavingPaymentHistory payment2 = createTestPaymentHistory(2L, 1, today.plusDays(1), new BigDecimal("200000"));
        
        // 상품 3: 어제 납입 예정 (처리되어야 함 - 과거)
        SavingSubscription subscription3 = createTestSubscription(1L, 3L, today.minusDays(1));
        SavingPaymentHistory payment3 = createTestPaymentHistory(3L, 1, today.minusDays(1), new BigDecimal("300000"));

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepo.findByUserIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(subscription1, subscription2, subscription3));
        
        // 각 구독별로 다른 납입 계획 반환
        when(paymentRepo.findNextPlannedPayment(1L)).thenReturn(Optional.of(payment1));
        when(paymentRepo.findNextPlannedPayment(2L)).thenReturn(Optional.of(payment2));
        when(paymentRepo.findNextPlannedPayment(3L)).thenReturn(Optional.of(payment3));
        
        when(savingTxnService.processSavingAutoDebit(anyLong(), anyString(), any(BigDecimal.class), anyLong()))
                .thenReturn(testWalletTransaction);

        // When
        autoDebitService.runOncePerDay(1L);

        // Then
        // 1) 상품 1, 3이 처리되어야 함 (오늘 + 과거 납입 예정)
        verify(savingTxnService, times(1))
                .processSavingAutoDebit(eq(1L), anyString(), eq(new BigDecimal("100000")), eq(1L));
        verify(savingTxnService, times(1))
                .processSavingAutoDebit(eq(1L), anyString(), eq(new BigDecimal("300000")), eq(3L));
        
        // 2) 상품 2는 처리되지 않아야 함 (미래 납입 예정)
        verify(savingTxnService, never())
                .processSavingAutoDebit(eq(1L), anyString(), eq(new BigDecimal("200000")), eq(2L));
        
        // 3) 상품 1, 3의 납입 이력이 저장되어야 함
        verify(paymentRepo, times(2)).save(paymentCaptor.capture());
        List<SavingPaymentHistory> savedPayments = paymentCaptor.getAllValues();
        
        // 상품 1 (오늘 납입 예정)
        SavingPaymentHistory payment1Saved = savedPayments.stream()
                .filter(p -> p.getSubscriptionId().equals(1L))
                .findFirst().orElseThrow();
        assertThat(payment1Saved.getStatus()).isEqualTo(SavingPaymentHistory.PaymentStatus.PAID);
        
        // 상품 3 (과거 납입 예정)
        SavingPaymentHistory payment3Saved = savedPayments.stream()
                .filter(p -> p.getSubscriptionId().equals(3L))
                .findFirst().orElseThrow();
        assertThat(payment3Saved.getStatus()).isEqualTo(SavingPaymentHistory.PaymentStatus.PAID);
    }

    @Test
    @DisplayName("여러 상품 가입 시 일부 실패 - 성공한 상품은 처리되고 실패한 상품은 미납 처리되어야 한다")
    void runOncePerDay_MultipleProducts_PartialFailure() {
        // Given
        // 상품 1: 성공
        SavingSubscription subscription1 = createTestSubscription(1L, 1L, today);
        SavingPaymentHistory payment1 = createTestPaymentHistory(1L, 1, today, new BigDecimal("100000"));
        
        // 상품 2: 실패 (잔액 부족)
        SavingSubscription subscription2 = createTestSubscription(1L, 2L, today);
        SavingPaymentHistory payment2 = createTestPaymentHistory(2L, 1, today, new BigDecimal("200000"));

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepo.findByUserIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(subscription1, subscription2));
        
        when(paymentRepo.findNextPlannedPayment(1L)).thenReturn(Optional.of(payment1));
        when(paymentRepo.findNextPlannedPayment(2L)).thenReturn(Optional.of(payment2));
        
        // 상품 1은 성공, 상품 2는 실패
        when(savingTxnService.processSavingAutoDebit(eq(1L), anyString(), eq(new BigDecimal("100000")), eq(1L)))
                .thenReturn(testWalletTransaction);
        when(savingTxnService.processSavingAutoDebit(eq(1L), anyString(), eq(new BigDecimal("200000")), eq(2L)))
                .thenThrow(new RuntimeException("잔액이 부족합니다"));
        
        when(paymentRepo.countBySubscriptionIdAndStatus(2L, SavingPaymentHistory.PaymentStatus.MISSED))
                .thenReturn(1L);

        // When
        autoDebitService.runOncePerDay(1L);

        // Then
        // 1) 두 상품 모두 처리 시도
        verify(savingTxnService, times(1))
                .processSavingAutoDebit(eq(1L), anyString(), eq(new BigDecimal("100000")), eq(1L));
        verify(savingTxnService, times(1))
                .processSavingAutoDebit(eq(1L), anyString(), eq(new BigDecimal("200000")), eq(2L));
        
        // 2) 두 상품 모두 저장 (성공: PAID, 실패: MISSED)
        verify(paymentRepo, times(2)).save(paymentCaptor.capture());
        List<SavingPaymentHistory> savedPayments = paymentCaptor.getAllValues();
        
        // 성공한 상품 (PAID)
        SavingPaymentHistory successPayment = savedPayments.stream()
                .filter(p -> p.getSubscriptionId().equals(1L))
                .findFirst().orElseThrow();
        assertThat(successPayment.getStatus()).isEqualTo(SavingPaymentHistory.PaymentStatus.PAID);
        
        // 실패한 상품 (MISSED)
        SavingPaymentHistory failedPayment = savedPayments.stream()
                .filter(p -> p.getSubscriptionId().equals(2L))
                .findFirst().orElseThrow();
        assertThat(failedPayment.getStatus()).isEqualTo(SavingPaymentHistory.PaymentStatus.MISSED);
    }

    // 테스트 헬퍼 메서드들
    private SavingSubscription createTestSubscription(Long userId, Long productId, LocalDate startDate) {
        SavingSubscription subscription = SavingSubscription.open(
                userId,
                productId,
                new AutoDebitAmount(new BigDecimal("100000")),
                new TermMonths(12),
                new ServiceDates(startDate, startDate.plusDays(11))
        );
        
        // 리플렉션으로 ID 세팅
        try {
            Field idField = SavingSubscription.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(subscription, productId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set subscription ID", e);
        }
        
        return subscription;
    }
    
    private SavingPaymentHistory createTestPaymentHistory(Long subscriptionId, Integer cycleNo, LocalDate dueDate, BigDecimal amount) {
        return SavingPaymentHistory.planned(subscriptionId, cycleNo, dueDate, amount);
    }
}
