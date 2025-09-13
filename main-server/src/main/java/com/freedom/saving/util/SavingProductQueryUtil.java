package com.freedom.saving.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 적금 상품 조회 관련 유틸리티 클래스
 * 정렬 옵션 검증, 파라미터 파싱 등의 공통 기능을 제공
 */
public class SavingProductQueryUtil {

    /**
     * 지원하는 정렬 옵션인지 검증
     * @param sort 정렬 옵션 문자열
     * @return 지원하는 정렬 옵션인지 여부
     */
    public static boolean isValidSortOption(String sort) {
        return "popular".equalsIgnoreCase(sort) || 
               "name".equalsIgnoreCase(sort);
    }
    
    /**
     * 은행사 파라미터를 파싱하여 리스트로 변환
     * @param banks 콤마로 구분된 은행사명 문자열 (예: "부산은행","경남은행")
     * @return 은행사명 리스트, null이거나 빈 문자열인 경우 빈 리스트 반환
     */
    public static List<String> parseBankNames(String banks) {
        if (banks == null || banks.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(banks.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();
    }
}
