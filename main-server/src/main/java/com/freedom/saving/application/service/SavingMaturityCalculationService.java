package com.freedom.saving.application.service;

import com.freedom.saving.domain.model.SavingProductOptionSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 적금 만기 금액 계산 서비스
 */
@Service
@RequiredArgsConstructor
public class SavingMaturityCalculationService {

    private static final int CALCULATION_SCALE = 10; // 이자 계산 시 사용할 소수점 자리수
    private static final BigDecimal MONTHS_OF_YEAR = new BigDecimal("12");
    private static final BigDecimal PERCENTAGE_DIVISOR = new BigDecimal("100");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.154"); // 이자소득세율 (15.4%)
    private static final String COMPOUND_INTEREST_TYPE_NAME = "복리";

    /**
     * 적금 만기 금액 계산 결과
     */
    public record MaturityCalculationResult(
            BigDecimal principal,           // 원금
            BigDecimal interest,            // 이자
            BigDecimal tax,                 // 세금 (이자소득세 15.4%)
            BigDecimal totalAmount,         // 총 만기 금액
            BigDecimal interestRate         // 적용 금리
    ) {}

    /**
     * 적금 만기 금액 계산
     * 
     * @param monthlyAmount 월 납입 금액
     * @param termMonths 적금 기간 (개월)
     * @param interestRate 연이율 (%)
     * @param isCompoundInterest 복리 여부 (true: 복리, false: 단리)
     * @return 만기 금액 계산 결과
     */
    public MaturityCalculationResult calculateMaturity(
            BigDecimal monthlyAmount,
            int termMonths,
            BigDecimal interestRate,
            boolean isCompoundInterest) {

        BigDecimal principal = monthlyAmount.multiply(BigDecimal.valueOf(termMonths));

        BigDecimal interest = isCompoundInterest ?
                calculateCompoundInterest(monthlyAmount, termMonths, interestRate) :
                calculateSimpleInterest(monthlyAmount, termMonths, interestRate);

        BigDecimal tax = interest.multiply(TAX_RATE).setScale(0, RoundingMode.DOWN);

        BigDecimal totalMaturityAmount = principal.add(interest).subtract(tax);

        return new MaturityCalculationResult(
                principal,
                interest.setScale(0, RoundingMode.DOWN),
                tax,
                totalMaturityAmount.setScale(0, RoundingMode.DOWN),
                interestRate
        );
    }

    /**
     * 복리 계산
     */
    private BigDecimal calculateCompoundInterest(BigDecimal monthlyAmount, int termMonths, BigDecimal annualRate) {

        BigDecimal monthlyRate = annualRate.divide(PERCENTAGE_DIVISOR, CALCULATION_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_OF_YEAR, CALCULATION_SCALE, RoundingMode.HALF_UP);

        BigDecimal principal = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (int i = 0; i < termMonths; i++) {
            principal = principal.add(monthlyAmount);
            totalInterest = totalInterest.add(principal.multiply(monthlyRate));
        }

        return totalInterest;
    }

    /**
     * 단리 계산
     */
    private BigDecimal calculateSimpleInterest(BigDecimal monthlyAmount, int termMonths, BigDecimal annualRate) {

        BigDecimal monthlyRate = annualRate.divide(PERCENTAGE_DIVISOR, CALCULATION_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_OF_YEAR, CALCULATION_SCALE, RoundingMode.HALF_UP);

        BigDecimal totalInterest = BigDecimal.ZERO;

        for (int i = 1; i <= termMonths; i++) {
            totalInterest = totalInterest.add(
                    monthlyAmount.multiply(monthlyRate).multiply(new BigDecimal(i))
            );
        }

        return totalInterest;
    }


    /**
     * 적금 상품 옵션으로부터 만기 금액 계산
     * 
     * @param monthlyAmount 월 납입 금액
     * @param option 적금 상품 옵션
     * @return 만기 금액 계산 결과
     */
    public MaturityCalculationResult calculateMaturityFromOption(
            BigDecimal monthlyAmount,
            SavingProductOptionSnapshot option) {

        if (option.getSaveTrmMonths() == null) {
            throw new IllegalArgumentException("적금 기간이 설정되지 않았습니다.");
        }

        BigDecimal interestRate = option.getIntrRate();
        if (interestRate == null) {
            throw new IllegalArgumentException("금리가 설정되지 않았습니다.");
        }

        boolean isCompoundInterest = !isSimpleInterest(option.getIntrRateTypeNm());

        return calculateMaturity(monthlyAmount, option.getSaveTrmMonths(), interestRate, isCompoundInterest);
    }

    /**
     * 우대 금리 적용 시 만기 금액 계산 (intr_rate2 사용)
     * 
     * @param monthlyAmount 월 납입 금액
     * @param option 적금 상품 옵션
     * @return 만기 금액 계산 결과
     */
    public MaturityCalculationResult calculateMaturityWithPreferentialRate(
            BigDecimal monthlyAmount,
            SavingProductOptionSnapshot option) {

        if (option.getSaveTrmMonths() == null) {
            throw new IllegalArgumentException("적금 기간이 설정되지 않았습니다.");
        }

        BigDecimal preferentialRate = option.getIntrRate2();
        if (preferentialRate == null) {
            throw new IllegalArgumentException("우대 금리가 설정되지 않았습니다.");
        }

        boolean isCompoundInterest = !isSimpleInterest(option.getIntrRateTypeNm());

        return calculateMaturity(monthlyAmount, option.getSaveTrmMonths(), preferentialRate, isCompoundInterest);
    }

    /**
     * 금리 타입명으로 단리/복리 구분
     */
    private boolean isSimpleInterest(String rateTypeName) {

        if (rateTypeName == null) {
            return true; // 기본값 단리
        }
        return !rateTypeName.contains(COMPOUND_INTEREST_TYPE_NAME);
    }
}
