package com.freedom.common.exception.custom;

import java.math.BigDecimal;

/**
 * 지갑 잔액 부족 예외
 */
public class InsufficientBalanceException extends RuntimeException {
    
    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;
    
    public InsufficientBalanceException(BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(String.format("잔액이 부족합니다. 현재 잔액: %s원, 요청 금액: %s원", 
                currentBalance, requestedAmount));
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }
    
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
    
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
