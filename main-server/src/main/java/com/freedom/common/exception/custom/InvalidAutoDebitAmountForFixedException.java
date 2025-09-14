package com.freedom.common.exception.custom;

import java.math.BigDecimal;

public class InvalidAutoDebitAmountForFixedException extends RuntimeException {
    public InvalidAutoDebitAmountForFixedException(BigDecimal amount) {
        super("정액적립식은 자동이체 금액이 필수이며 0보다 커야 합니다. amount=" + amount);
    }
}
