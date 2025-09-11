package com.freedom.auth.api.dto;

import lombok.Getter;

@Getter
public class AdminAuthCheckResponse {
    private boolean authenticated;
    private String message;

    public AdminAuthCheckResponse() {}

    /**
     * 인증된 상태 응답
     */
    public static AdminAuthCheckResponse authenticated() {
        AdminAuthCheckResponse response = new AdminAuthCheckResponse();
        response.authenticated = true;
        response.message = "인증됨";
        return response;
    }
}
