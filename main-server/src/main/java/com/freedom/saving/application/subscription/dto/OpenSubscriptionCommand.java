package com.freedom.saving.application.subscription.dto;

import lombok.Getter;

import java.math.BigDecimal;

import static com.freedom.common.exception.custom.SavingExceptions.*;

/**
 * 가입 요청 입력 모델(애플리케이션 계층)
 * - 기간(termMonths)은 필수: 사용자가 상품 옵션에서 선택
 * - 적립유형은 정액적립식으로 고정
 */
@Getter
public class OpenSubscriptionCommand {

    private final Long userId;
    private final Long productSnapshotId;
    private final Integer termMonths;          // 필수: 사용자가 선택한 기간
    private final BigDecimal autoDebitAmount;  // 정액적립식이므로 필수(>0)

    public OpenSubscriptionCommand(Long userId,
                                   Long productSnapshotId,
                                   Integer termMonths,
                                   BigDecimal autoDebitAmount) {
        if (userId == null || userId.longValue() <= 0L) {
            throw new SavingPaymentInvalidParamsException("userId는 필수입니다.");
        }
        if (productSnapshotId == null || productSnapshotId.longValue() <= 0L) {
            throw new SavingPaymentInvalidParamsException("productSnapshotId는 필수입니다.");
        }
        if (termMonths == null || termMonths.intValue() <= 0) {
            throw new SavingPaymentInvalidParamsException("termMonths는 필수입니다.");
        }
        this.userId = userId;
        this.productSnapshotId = productSnapshotId;
        this.termMonths = termMonths;      // 필수값
        this.autoDebitAmount = autoDebitAmount; // 정액적립식이므로 필수
    }

}
