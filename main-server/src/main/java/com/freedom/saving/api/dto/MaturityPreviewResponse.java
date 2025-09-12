package com.freedom.saving.api.dto;

import com.freedom.saving.application.SavingMaturityCalculationService.MaturityCalculationResult;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 적금 만기 금액 미리보기 응답
 */
@Getter
@NoArgsConstructor
public class MaturityPreviewResponse {

    private MaturityInfo ourService;
    private MaturityInfo preferentialRate;
    private String productName;
    private String bankName;

    public MaturityPreviewResponse(MaturityInfo ourService, MaturityInfo preferentialRate,
                                   String productName, String bankName) {
        this.ourService = ourService;
        this.preferentialRate = preferentialRate;
        this.productName = productName;
        this.bankName = bankName;
    }

    /**
     * 만기 금액 정보
     */
    @Getter
    @NoArgsConstructor
    public static class MaturityInfo {
        private BigDecimal principal;      // 원금
        private BigDecimal interest;       // 예상 이자
        private BigDecimal tax;            // 세금
        private BigDecimal totalAmount;    // 총 금액
        private BigDecimal interestRate;   // 적용 금리

        public MaturityInfo(BigDecimal principal, BigDecimal interest, BigDecimal tax, 
                          BigDecimal totalAmount, BigDecimal interestRate) {
            this.principal = principal;
            this.interest = interest;
            this.tax = tax;
            this.totalAmount = totalAmount;
            this.interestRate = interestRate;
        }

        public static MaturityInfo from(MaturityCalculationResult result) {
            return new MaturityInfo(
                    result.principal(),
                    result.interest(),
                    result.tax(),
                    result.totalAmount(),
                    result.interestRate()
            );
        }
    }
}
