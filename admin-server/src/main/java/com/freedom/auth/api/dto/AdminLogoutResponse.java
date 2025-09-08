package com.freedom.auth.api.dto;

import lombok.Getter;

@Getter
public class AdminLogoutResponse {
    private boolean success;
    private String message;
    private String redirectUrl;

    /**
     * 성공 응답 생성
     */
    public static AdminLogoutResponse success(String redirectUrl) {
        AdminLogoutResponse response = new AdminLogoutResponse();
        response.success = true;
        response.message = "로그아웃 성공";
        response.redirectUrl = redirectUrl;
        return response;
    }

    /**
     * 실패 응답 생성 (거의 사용되지 않음)
     */
    public static AdminLogoutResponse failure(String message) {
        AdminLogoutResponse response = new AdminLogoutResponse();
        response.success = false;
        response.message = message;
        return response;
    }
}
