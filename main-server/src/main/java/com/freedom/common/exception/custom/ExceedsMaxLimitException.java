package com.freedom.common.exception.custom;

import java.math.BigDecimal;

/**
 * 최고한도 초과 예외
 */
public class ExceedsMaxLimitException extends RuntimeException {
    
    private final BigDecimal requestedAmount;
    private final BigDecimal maxLimit;
    
    public ExceedsMaxLimitException(BigDecimal requestedAmount, BigDecimal maxLimit) {
        super(String.format("요청 금액이 최고 한도를 초과합니다. 요청 금액: %s원, 최고 한도: %s원",
                requestedAmount, maxLimit));
        this.requestedAmount = requestedAmount;
        this.maxLimit = maxLimit;
    }
    
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
    
    public BigDecimal getMaxLimit() {
        return maxLimit;
    }
}
