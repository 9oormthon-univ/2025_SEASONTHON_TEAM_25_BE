package com.freedom.saving.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 적금 만기 금액 미리보기 요청
 */
@Getter
@NoArgsConstructor
public class MaturityPreviewRequest {

    @NotNull(message = "월 납입 금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "월 납입 금액은 0원보다 커야 합니다.")
    private BigDecimal monthlyAmount;

    @NotNull(message = "적금 기간은 필수입니다.")
    @Min(value = 1, message = "적금 기간은 1개월 이상이어야 합니다.")
    private Integer termMonths;

    public MaturityPreviewRequest(BigDecimal monthlyAmount, Integer termMonths) {
        this.monthlyAmount = monthlyAmount;
        this.termMonths = termMonths;
    }
}