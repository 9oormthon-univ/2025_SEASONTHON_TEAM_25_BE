package com.freedom.saving.domain.entities;

import com.freedom.saving.domain.subscription.AutoDebitAmount;
import com.freedom.saving.domain.subscription.ServiceDates;
import com.freedom.saving.domain.subscription.SubscriptionStatus;
import com.freedom.saving.domain.subscription.TermMonths;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.freedom.common.exception.custom.SavingExceptions.*;

/**
 * 가입 애그리거트 루트
 * 외부 연관은 모두 식별자(Long)로 보관
 */
@Getter
@Entity
@Table(name = "saving_subscription",
        indexes = {
                @Index(name = "idx_sub_user", columnList = "user_id"),
                @Index(name = "idx_sub_product", columnList = "product_snapshot_id"),
                @Index(name = "idx_sub_status", columnList = "status")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavingSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소유자/상품 식별자만 저장
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_snapshot_id", nullable = false)
    private Long productSnapshotId;

    @Embedded
    private AutoDebitAmount autoDebitAmount;

    @Embedded
    private TermMonths term;

    @Embedded
    private ServiceDates dates;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SubscriptionStatus status;

    // ====== 생성/상태전이 ======

    /**
     * 가입 생성 팩토리
     * - 상태는 ACTIVE 로 시작
     * - 불변/일관성 유지를 위해 new 대신 사용
     */
    public static SavingSubscription open(Long userId,
                                          Long productSnapshotId,
                                          AutoDebitAmount amount,
                                          TermMonths term,
                                          ServiceDates dates) {
        if (userId == null || productSnapshotId == null) {
            throw new SavingSnapshotIdentifiersInvalidException();
        }
        SavingSubscription s = new SavingSubscription();
        s.userId = userId;
        s.productSnapshotId = productSnapshotId;
        s.autoDebitAmount = amount;
        s.term = term;
        s.dates = dates;
        s.status = SubscriptionStatus.ACTIVE;
        return s;
    }

    /** 사용자 자발적 해지 */
    public void cancelByUser() {
        ensureActive();
        this.status = SubscriptionStatus.CANCELLED;
    }

    /** 정책 강제 해지 */
    public void forceCancel() {
        ensureActive();
        this.status = SubscriptionStatus.FORCED_CANCELLED;
    }

    /** 만기 확정(자동이체/정산 완료 이후) */
    public void mature() {
        ensureActive();
        this.status = SubscriptionStatus.MATURED;
    }

    private void ensureActive() {
        if (this.status != SubscriptionStatus.ACTIVE) {
            throw new SavingSubscriptionInvalidStateException(this.status.name());
        }
    }
}
