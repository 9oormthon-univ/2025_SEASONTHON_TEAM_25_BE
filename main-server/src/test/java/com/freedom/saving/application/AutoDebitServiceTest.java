package com.freedom.saving.application;

import com.freedom.auth.domain.User;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.saving.domain.payment.SavingPaymentHistory;
import com.freedom.saving.domain.payment.SavingPaymentHistoryRepository;
import com.freedom.saving.domain.subscription.AutoDebitAmount;
import com.freedom.saving.domain.subscription.SavingSubscription;
import com.freedom.saving.domain.subscription.ServiceDates;
import com.freedom.saving.domain.subscription.SubscriptionStatus;
import com.freedom.saving.domain.subscription.TermMonths;
import com.freedom.saving.infra.snapshot.SavingSubscriptionJpaRepository;
import com.freedom.wallet.application.SavingTransactionService;
import com.freedom.wallet.domain.UserWallet;
import com.freedom.wallet.domain.WalletTransaction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

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
    @Mock private PlatformTransactionManager txManager;
    @Mock private TransactionStatus transactionStatus;

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
        // 어제 처리된 상태로 세팅 -> 오늘은 처리 대상
        testUser.updateLastAutoPaymentDate(today.minusDays(1));

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

        // 2) 유저의 '오늘 처리 일자' 업데이트 확인
        verify(userRepo, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getLastAutoPaymentDate())
                .as("성공 시 오늘 날짜로 최종 자동납입 처리 일자가 갱신되어야 한다")
                .isEqualTo(today);

        // 3) 부가적 행위 검증(필요 최소한)
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

        // 2) 유저의 마지막 처리 일자도 오늘로 업데이트되어 재시도 루프를 막아야 함
        verify(userRepo, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getLastAutoPaymentDate()).isEqualTo(today);

        // 3) 미납 카운트 조회가 수행되었는지 확인(강제해지 판단 로직)
        verify(paymentRepo, times(1))
                .countBySubscriptionIdAndStatus(1L, SavingPaymentHistory.PaymentStatus.MISSED);
    }

    @Test
    @DisplayName("이미 오늘 자동납입이 처리된 경우 - 스킵되어야 한다")
    void runOncePerDay_AlreadyProcessedToday_Skip() {
        // Given
        testUser.updateLastAutoPaymentDate(today); // 이미 오늘 처리됨
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        autoDebitService.runOncePerDay(1L);

        // Then
        // 1) 추가 처리(결제/히스토리 저장)가 없어야 함
        verify(subscriptionRepo, never()).findByUserIdAndStatus(anyLong(), any(SubscriptionStatus.class));
        verify(savingTxnService, never()).processSavingAutoDebit(anyLong(), anyString(), any(BigDecimal.class), anyLong());
        verify(paymentRepo, never()).save(any(SavingPaymentHistory.class));

        // 2) 여전히 오늘로 표시되어 있어야 함
        assertThat(testUser.getLastAutoPaymentDate())
                .as("이미 처리된 경우 상태 변화 없어야 한다")
                .isEqualTo(today);
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

        // 2) 오늘 처리로 마킹(다시 호출되는 것을 방지)
        verify(userRepo, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getLastAutoPaymentDate())
                .as("활성 구독이 없어도 '오늘 확인 완료' 상태로 마킹해 중복 호출 방지")
                .isEqualTo(today);
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

        // 2) 오늘 처리로 마킹
        verify(userRepo, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getLastAutoPaymentDate()).isEqualTo(today);
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

        // 3) 오늘 처리로 마킹
        verify(userRepo, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getLastAutoPaymentDate()).isEqualTo(today);
    }
}
