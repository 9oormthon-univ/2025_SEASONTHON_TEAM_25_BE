package com.freedom.saving.util;

/**
 * 가입제한 코드를 의미있는 문자열로 변환하는 유틸리티 클래스
 */
public class JoinDenyConverter {

    /**
     * 가입제한 코드를 문자열로 변환
     * @param joinDenyCode 가입제한 코드 (1: 제한없음, 2: 서민전용, 3: 일부제한)
     * @return 변환된 가입제한 문자열
     */
    public static String convertJoinDeny(String joinDenyCode) {
        if (joinDenyCode == null || joinDenyCode.trim().isEmpty()) {
            return "정보없음";
        }
        
        switch (joinDenyCode.trim()) {
            case "1":
                return "제한없음";
            case "2":
                return "서민전용";
            case "3":
                return "일부제한";
            default:
                return "정보없음";
        }
    }
}
