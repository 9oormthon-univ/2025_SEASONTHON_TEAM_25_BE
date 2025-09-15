package com.freedom.saving.application;

import com.freedom.saving.domain.SavingProductOptionSnapshot;
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
        
        // 원금 계산
        BigDecimal principal = monthlyAmount.multiply(BigDecimal.valueOf(termMonths));
        
        BigDecimal interest;
        
        if (isCompoundInterest) {
            // 복리 계산
            interest = calculateCompoundInterest(monthlyAmount, termMonths, interestRate);
        } else {
            // 단리 계산
            interest = calculateSimpleInterest(monthlyAmount, termMonths, interestRate);
        }
        
        // 이자소득세 계산 (15.4%)
        BigDecimal taxRate = new BigDecimal("0.154");
        BigDecimal tax = interest.multiply(taxRate).setScale(0, RoundingMode.DOWN);
        
        // 총 만기 금액
        BigDecimal totalMaturityAmount = principal.add(interest).subtract(tax);
        
        MaturityCalculationResult result = new MaturityCalculationResult(
                principal,
                interest.setScale(0, RoundingMode.DOWN),
                tax,
                totalMaturityAmount.setScale(0, RoundingMode.DOWN),
                interestRate
        );
        
        
        return result;
    }

    /**
     * 복리 계산
     */
    private BigDecimal calculateCompoundInterest(BigDecimal monthlyAmount, int termMonths, BigDecimal interestRate) {
        BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        
        BigDecimal currentBalance = BigDecimal.ZERO;
        
        // 매월 복리 계산
        for (int month = 1; month <= termMonths; month++) {
            currentBalance = currentBalance.add(monthlyAmount);
            BigDecimal monthlyInterest = currentBalance.multiply(monthlyRate);
            currentBalance = currentBalance.add(monthlyInterest);
        }
        
        BigDecimal principal = monthlyAmount.multiply(BigDecimal.valueOf(termMonths));
        return currentBalance.subtract(principal);
    }

    /**
     * 단리 계산
     */
    private BigDecimal calculateSimpleInterest(BigDecimal monthlyAmount, int termMonths, BigDecimal interestRate) {
        BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        
        BigDecimal totalInterest = BigDecimal.ZERO;
        
        // 매월 단리 계산 (매월 납입액에 대해서만 이자 계산)
        for (int month = 1; month <= termMonths; month++) {
            BigDecimal monthlyInterest = monthlyAmount.multiply(monthlyRate).multiply(BigDecimal.valueOf(termMonths - month + 1));
            totalInterest = totalInterest.add(monthlyInterest);
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
        
        // 단리/복리 구분
        boolean isCompoundInterest = isCompoundInterest(option.getIntrRateTypeNm());
        
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
        
        // 단리/복리 구분
        boolean isCompoundInterest = isCompoundInterest(option.getIntrRateTypeNm());
        
        return calculateMaturity(monthlyAmount, option.getSaveTrmMonths(), preferentialRate, isCompoundInterest);
    }

    /**
     * 금리 타입명으로 단리/복리 구분
     * 
     * @param rateTypeName 금리 타입명
     * @return true: 복리, false: 단리
     */
    private boolean isCompoundInterest(String rateTypeName) {
        if (rateTypeName == null) {
            return true; // 기본값은 복리
        }
        
        // "단리"가 포함되어 있으면 단리, 그 외에는 복리
        return !rateTypeName.contains("단리");
    }
}
