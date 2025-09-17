package com.freedom.saving.application.query.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 상세 응답용 dto
 */
@Getter
@Setter
public class SavingProductDetail {

    // 기본 정보
    private String productName;
    private String bankName;
    private String maturityInterest; // 만기 후 이자율
    private String specialCondition; // 우대조건
    private String joinDeny; // 가입제한
    private String joinMember; // 가입대상
    private Integer maxLimit; // 최고한도
    
    // 금리 정보
    private String intrRateType; // 저축 금리 유형
    private String intrRateTypeNm; // 저축 금리 유형명
    private String rsrvType; // 적립 유형
    private String rsrvTypeNm; // 적립 유형명
    private List<Integer> saveTrm; // 저축 기간 목록 (개월)
    private List<BigDecimal> intrRate; // 저축 금리 목록
    private List<BigDecimal> intrRate2; // 최고 우대금리 목록
}
