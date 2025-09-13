package com.freedom.saving.application.read;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 상세 응답용 dto
 */
@Getter
@Setter
public class SavingProductDetail {

    // 기본 정보
    private Long productSnapshotId;
    private String productName;
    private String bankName;
    private String joinWay;
    private String maturityInterest; // 만기 후 이자율
    private String specialCondition;
    private String joinDeny;
    private String joinMember;
    private Integer maxLimit; // 최고한도
    private String etcNote;
    private String aiSummary;
    private LocalDateTime fetchedAt;
    
    // 히어로 섹션용 정보
    private String interestRateRange; // "연 2.25%~3.55%" 형식
    private String availableTerms; // "12/24/36개월 중 선택" 형식
    private String maxMonthlyAmount; // "회당 최대 N,NNN,NNN원" 형식
    
    // 상품 정보 자세히 보기 섹션용 정보
    private String financialCompanyName; // 금융회사 명
    private String financialProductName; // 금융 상품명
    private String maturityInterestRate; // 만기 후 이자율
    private String preferentialConditions; // 우대조건
    private String joinRestrictions; // 가입제한
    private String joinTarget; // 가입대상
    private String maxLimitFormatted; // 최고한도 (포맷된 형태)
    private String savingsRateType; // 저축 금리 유형
    private String savingsRateTypeName; // 저축 금리 유형명
    private String depositType; // 적립 유형
    private String depositTypeName; // 적립 유형명
    private List<Integer> availableTermMonths; // 저축 기간 목록
    private List<BigDecimal> savingsRates; // 저축 금리 목록 (소수점 2자리)
    private List<BigDecimal> maxPreferentialRates; // 최고 우대금리 목록 (소수점 2자리)
    
    // 옵션 목록
    private List<SavingProductOptionItem> options = new ArrayList<>();
    
    // 만기 예상 금액 계산용 기본값
    private BigDecimal defaultMonthlyAmount = new BigDecimal("100000"); // 기본 월납입금액
    private Integer defaultTermMonths = 12; // 기본 기간
}
