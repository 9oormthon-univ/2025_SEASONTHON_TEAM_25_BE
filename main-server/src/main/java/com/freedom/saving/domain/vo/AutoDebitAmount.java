package com.freedom.saving.domain.vo;


import com.freedom.common.exception.custom.InvalidAmountException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 자동이체 금액 VO
 * - 통화는 원화 전제, 소수 불허(정수 원 단위)
 * - 불변 + 자체 검증으로 무결성 보장
 */

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AutoDebitAmount {

    @Column(name = "auto_debit_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal value;

    public AutoDebitAmount(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new InvalidAmountException("자동이체 금액은 0보다 커야 합니다.");
        }
        // 원 단위 고정
        this.value = value.setScale(0, RoundingMode.DOWN);
    }

    public long toLong() {
        return this.value.longValue();
    }
}
