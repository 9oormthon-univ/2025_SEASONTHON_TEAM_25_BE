package com.freedom.wallet.domain;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 지갑 거래 이력 엔티티
 * - 모든 입출금 거래를 기록
 * - 멱등성 보장을 위한 request_id 포함
 * - FK 참조 무결성 보장
 */
@Getter
@Entity
@Table(name = "wallet_transaction",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wallet_transaction_request_id", columnNames = {"request_id"})
        },
        indexes = {
                @Index(name = "idx_wallet_transaction_wallet_created", columnList = "wallet_id, created_at"),
                @Index(name = "idx_wallet_transaction_direction", columnList = "direction"),
                @Index(name = "idx_wallet_transaction_reason", columnList = "reason_code")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wallet_transaction_wallet"))
    private UserWallet wallet;

    @Column(name = "request_id", nullable = false, unique = true, length = 100)
    private String requestId; // 멱등키

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private TransactionDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false, length = 30)
    private TransactionReasonCode reasonCode; // 거래 사유 코드

    @Column(name = "amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal amount; // 거래 금액 (원 단위)

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 0)
    private BigDecimal balanceAfter; // 거래 후 잔액

    @Column(name = "description", length = 500)
    private String description; // 거래 설명

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // 관련 엔티티 타입 (예: SAVING_SUBSCRIPTION)

    @Column(name = "related_entity_id")
    private Long relatedEntityId; // 관련 엔티티 ID

    /**
     * 거래 방향 열거형
     */
    public enum TransactionDirection {
        DEPOSIT,  // 입금
        WITHDRAW    // 출금
    }


    /**
     * 거래 생성 팩토리 - 적금 가입
     */
    public static WalletTransaction createSavingJoin(UserWallet wallet, String requestId, BigDecimal amount, 
                                                    Long subscriptionId) {
        return create(wallet, requestId, TransactionDirection.WITHDRAW, TransactionReasonCode.SAVING_JOIN,
                     amount, "적금 가입", "SAVING_SUBSCRIPTION", subscriptionId);
    }

    /**
     * 거래 생성 팩토리 - 적금 해제
     */
    public static WalletTransaction createSavingCancel(UserWallet wallet, String requestId, BigDecimal amount, 
                                                      Long subscriptionId) {
        return create(wallet, requestId, TransactionDirection.DEPOSIT, TransactionReasonCode.SAVING_CANCEL,
                     amount, "적금 해제", "SAVING_SUBSCRIPTION", subscriptionId);
    }

    /**
     * 거래 생성 팩토리 - 적금 만기
     */
    public static WalletTransaction createSavingMaturity(UserWallet wallet, String requestId, BigDecimal amount, 
                                                        Long subscriptionId) {
        return create(wallet, requestId, TransactionDirection.DEPOSIT, TransactionReasonCode.SAVING_MATURITY,
                     amount, "적금 만기", "SAVING_SUBSCRIPTION", subscriptionId);
    }

    /**
     * 거래 생성 팩토리 - 적금 이자
     */
    public static WalletTransaction createSavingInterest(UserWallet wallet, String requestId, BigDecimal amount, 
                                                        Long subscriptionId) {
        return create(wallet, requestId, TransactionDirection.DEPOSIT, TransactionReasonCode.SAVING_INTEREST,
                     amount, "적금 이자", "SAVING_SUBSCRIPTION", subscriptionId);
    }

    /**
     * 거래 생성 팩토리 - 적금 자동 납입
     */
    public static WalletTransaction createSavingAutoDebit(UserWallet wallet, String requestId, BigDecimal amount,
                                                          Long subscriptionId) {
        return create(wallet, requestId, TransactionDirection.WITHDRAW, TransactionReasonCode.SAVING_AUTO_DEBIT,
                amount, "적금 자동 납입", "SAVING_SUBSCRIPTION", subscriptionId);
    }

    /**
     * 거래 생성 팩토리 - 적금 수동 납입
     */
    public static WalletTransaction createSavingManualPayment(UserWallet wallet, String requestId, BigDecimal amount,
                                                              Long subscriptionId) {
        return create(wallet, requestId, TransactionDirection.WITHDRAW, TransactionReasonCode.SAVING_MANUAL_PAYMENT,
                amount, "적금 수동 납입", "SAVING_SUBSCRIPTION", subscriptionId);
    }

    /**
     * 공통 거래 생성 메서드
     */
    private static WalletTransaction create(UserWallet wallet, String requestId, TransactionDirection direction, 
                                           TransactionReasonCode reasonCode, BigDecimal amount, String description, 
                                           String relatedEntityType, Long relatedEntityId) {
        if (wallet == null) {
            throw new IllegalArgumentException("지갑은 필수입니다.");
        }
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("요청 ID는 필수입니다.");
        }
        if (direction == null) {
            throw new IllegalArgumentException("거래 방향은 필수입니다.");
        }
        if (reasonCode == null) {
            throw new IllegalArgumentException("거래 사유 코드는 필수입니다.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("거래 금액은 0보다 커야 합니다.");
        }

        WalletTransaction transaction = new WalletTransaction();
        transaction.wallet = wallet;
        transaction.requestId = requestId;
        transaction.direction = direction;
        transaction.reasonCode = reasonCode;
        transaction.amount = amount;
        transaction.balanceAfter = wallet.getBalance();
        transaction.description = description;
        transaction.relatedEntityType = relatedEntityType;
        transaction.relatedEntityId = relatedEntityId;
        return transaction;
    }

    /**
     * 거래가 입금인지 확인
     */
    public boolean isCredit() {
        return direction == TransactionDirection.DEPOSIT;
    }

    /**
     * 거래가 출금인지 확인
     */
    public boolean isDebit() {
        return direction == TransactionDirection.WITHDRAW;
    }

    /**
     * 지갑 ID 조회 (편의 메서드)
     */
    public Long getWalletId() {
        return wallet != null ? wallet.getId() : null;
    }
}