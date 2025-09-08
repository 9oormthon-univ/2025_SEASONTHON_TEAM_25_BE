package com.freedom.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freedom.common.exception.ErrorCode;
import com.freedom.common.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * JWT 인증 실패 시 401 Unauthorized 응답을 반환하는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException, ServletException {

        String uri = request.getRequestURI();
        String xhr = request.getHeader("X-Requested-With");

        boolean isAjax = xhr != null && "XMLHttpRequest".equalsIgnoreCase(xhr);
        boolean isAdminPage = uri != null && uri.startsWith("/admin");

        // 브라우저 페이지 접근(비-AJAX)이고 관리자 경로이면 리다이렉트 기반 처리
        if (!isAjax && isAdminPage) {
            boolean hasRefreshCookie = hasCookie(request, "admin_refresh_token");
            if (hasRefreshCookie) {
                String original = buildOriginalUrl(request);
                String redirectUrl = "/admin/api/auth/refresh?redirect=" + URLEncoder.encode(original, StandardCharsets.UTF_8);
                response.sendRedirect(redirectUrl);
                return;
            } else {
                // 쿠키 정리 후 로그인 페이지로
                clearCookie(response, "admin_access_token");
                clearCookie(response, "admin_refresh_token");
                response.sendRedirect("/admin/login");
                return;
            }
        }

        log.warn("인증되지 않은 사용자의 접근: {} {}", request.getMethod(), request.getRequestURI());

        // JSON 응답 (AJAX 또는 API 요청)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.TOKEN_INVALID);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    private boolean hasCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return false;
        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void clearCookie(HttpServletResponse response, String name) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath("/admin");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }

    private String buildOriginalUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String qs = request.getQueryString();
        if (qs == null || qs.isEmpty()) return uri;
        return uri + "?" + qs;
    }
}
