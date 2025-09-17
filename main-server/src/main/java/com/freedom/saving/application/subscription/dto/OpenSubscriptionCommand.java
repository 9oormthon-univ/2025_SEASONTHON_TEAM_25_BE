package com.freedom.saving.application.subscription.dto;

import java.math.BigDecimal;

import static com.freedom.common.exception.custom.SavingExceptions.*;

/**
 * 가입 요청 입력 모델(애플리케이션 계층)
 * - 기간(termMonths)은 필수: 사용자가 상품 옵션에서 선택
 * - 적립유형은 정액적립식으로 고정
 *
 * @param termMonths      필수: 사용자가 선택한 기간
 * @param autoDebitAmount 정액적립식이므로 필수(>0)
 */
public record OpenSubscriptionCommand(Long userId, Long productSnapshotId, Integer termMonths,
                                      BigDecimal autoDebitAmount) {

    public OpenSubscriptionCommand {
        if (userId == null || userId <= 0L) {
            throw new SavingPaymentInvalidParamsException("userId는 필수입니다.");
        }
        if (productSnapshotId == null || productSnapshotId <= 0L) {
            throw new SavingPaymentInvalidParamsException("productSnapshotId는 필수입니다.");
        }
        if (termMonths == null || termMonths <= 0) {
            throw new SavingPaymentInvalidParamsException("termMonths는 필수입니다.");
        }
    }

}
