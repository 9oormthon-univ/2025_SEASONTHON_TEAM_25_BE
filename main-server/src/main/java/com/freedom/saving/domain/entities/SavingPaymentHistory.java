package com.freedom.saving.domain.entities;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.freedom.common.exception.custom.SavingExceptions.*;

@Getter
@Entity
@Table(
        name = "saving_payment_history",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_sub_cycle", columnNames = {"subscription_id", "cycle_no"})
        },
        indexes = {
                @Index(name = "idx_payment_subscription_id", columnList = "subscription_id"),
                @Index(name = "idx_payment_due_date", columnList = "due_service_date"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_subscription_due", columnList = "subscription_id, due_service_date DESC"),
                @Index(name = "idx_payment_sub_status_cycle", columnList = "subscription_id, status, cycle_no")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavingPaymentHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "cycle_no")
    private Integer cycleNo;

    @Column(name = "due_service_date")
    private LocalDate dueServiceDate;

    @Column(name = "expected_amount", precision = 15, scale = 0)
    private BigDecimal expectedAmount;

    public enum PaymentStatus { PLANNED, PAID, PARTIAL, MISSED }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private PaymentStatus status;

    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "wallet_txn_id")
    private Long walletTxnId; // Wallet 연동 (FK)

    @Column(name = "description", length = 500)
    private String description;

    public static SavingPaymentHistory planned(Long subscriptionId, Integer cycleNo, LocalDate dueDate, BigDecimal expectedAmount) {
        if (subscriptionId == null) throw new SavingPaymentInvalidParamsException("subscriptionId is required");
        if (cycleNo == null || cycleNo <= 0) throw new SavingPaymentInvalidParamsException("cycleNo must be >= 1");
        if (dueDate == null) throw new SavingPaymentInvalidParamsException("dueServiceDate is required");
        if (expectedAmount == null || expectedAmount.signum() <= 0) throw new SavingPaymentInvalidParamsException("expectedAmount must be > 0");
        SavingPaymentHistory h = new SavingPaymentHistory();
        h.subscriptionId = subscriptionId;
        h.cycleNo = cycleNo;
        h.dueServiceDate = dueDate;
        h.expectedAmount = expectedAmount;
        h.status = PaymentStatus.PLANNED;
        h.paidAmount = BigDecimal.ZERO;
        h.attemptCount = 0;
        return h;
    }

    public void markPaid(BigDecimal amount, Long walletTxnId, LocalDateTime paidAt) {
        if (amount == null || amount.signum() <= 0) throw new SavingInvalidPaymentAmountException();
        this.paidAmount = amount;
        this.paidAt = paidAt != null ? paidAt : LocalDateTime.now();
        this.walletTxnId = walletTxnId;
        this.status = amount.compareTo(expectedAmount != null ? expectedAmount : amount) >= 0 ? PaymentStatus.PAID : PaymentStatus.PARTIAL;
        this.attemptCount = this.attemptCount + 1;
        this.lastAttemptAt = LocalDateTime.now();
    }

    public void markMissed() {
        this.status = PaymentStatus.MISSED;
        this.attemptCount = this.attemptCount + 1;
        this.lastAttemptAt = LocalDateTime.now();
    }

}
