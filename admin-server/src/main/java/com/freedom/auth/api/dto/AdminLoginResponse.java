package com.freedom.auth.api.dto;

import lombok.Getter;

@Getter
public class AdminLoginResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String username;
    private String redirectUrl;

    /**
     * 성공 응답 생성
     */
    public static AdminLoginResponse success(String accessToken, String username, String redirectUrl) {
        AdminLoginResponse response = new AdminLoginResponse();
        response.success = true;
        response.message = "로그인 성공";
        response.accessToken = accessToken;
        response.username = username;
        response.redirectUrl = redirectUrl;
        return response;
    }

    /**
     * 실패 응답 생성
     */
    public static AdminLoginResponse failure(String message) {
        AdminLoginResponse response = new AdminLoginResponse();
        response.success = false;
        response.message = message;
        return response;
    }
}
