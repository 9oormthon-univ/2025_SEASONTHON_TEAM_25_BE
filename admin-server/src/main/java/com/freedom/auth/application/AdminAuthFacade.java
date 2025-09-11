package com.freedom.auth.application;

import com.freedom.auth.api.dto.AdminLoginResponse;
import com.freedom.auth.api.dto.AdminLogoutResponse;
import com.freedom.auth.application.dto.TokenDto;
import com.freedom.auth.domain.UserRole;
import com.freedom.auth.domain.service.AdminAuthService;
import com.freedom.common.exception.custom.InvalidPasswordException;
import com.freedom.common.exception.custom.UserNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 관리자 인증 Facade 서비스
 * - 인증 비즈니스 플로우 조율
 * - Cookie 처리 및 HTTP 응답 관리
 * - 인증 관련 복합 로직 처리
 */
@Service
@RequiredArgsConstructor
public class AdminAuthFacade {

    private final AdminAuthService adminAuthService;

    /**
     * 관리자 로그인 처리
     */
    public ResponseEntity<AdminLoginResponse> handleLogin(String email, String password, HttpServletResponse response) {
        try {
            TokenDto tokenDto = adminAuthService.login(email, password);
            
            // 관리자 권한 검증
            if (!isAdminRole(tokenDto)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(AdminLoginResponse.failure("관리자 권한이 필요합니다."));
            }

            // 인증 쿠키 설정
            setAuthCookies(response, tokenDto.getAccessToken(), tokenDto.getRefreshToken());

            return ResponseEntity.ok(AdminLoginResponse.success(
                tokenDto.getAccessToken(),
                tokenDto.getUser().getEmail(),
                "/admin/dashboard"
            ));

        } catch (UserNotFoundException | InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AdminLoginResponse.failure("이메일 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    /**
     * 관리자 로그아웃 처리
     */
    public ResponseEntity<AdminLogoutResponse> handleLogout(HttpServletResponse response) {
        clearAuthCookies(response);
        return ResponseEntity.ok(AdminLogoutResponse.success("/admin/login"));
    }

    /**
     * 토큰 갱신 처리
     */
    public ResponseEntity<AdminLoginResponse> handleTokenRefresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AdminLoginResponse.failure("리프레시 토큰이 없습니다."));
        }

        try {
            TokenDto tokenDto = adminAuthService.refreshTokens(refreshToken);
            setAuthCookies(response, tokenDto.getAccessToken(), tokenDto.getRefreshToken());
            
            // 리다이렉트 처리
            String redirectUrl = request.getParameter("redirect");
            if (shouldRedirect(redirectUrl)) {
                try {
                    response.sendRedirect(redirectUrl);
                    return null; // 이미 응답 처리됨
                } catch (IOException ignore) {
                    // 리다이렉트 실패 시 일반 응답으로 처리
                }
            }
            
            return ResponseEntity.ok(AdminLoginResponse.success(
                tokenDto.getAccessToken(),
                tokenDto.getUser().getEmail(),
                "/admin/dashboard"
            ));
            
        } catch (Exception e) {
            clearAuthCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AdminLoginResponse.failure("리프레시 토큰이 유효하지 않거나 만료되었습니다."));
        }
    }

    /**
     * 관리자 권한 검증
     */
    private boolean isAdminRole(TokenDto tokenDto) {
        return UserRole.ADMIN.name().equals(tokenDto.getUser().getRole());
    }

    /**
     * 리프레시 토큰 추출
     */
    private String extractRefreshToken(HttpServletRequest request) {
        return extractCookie(request, "admin_refresh_token");
    }

    /**
     * 리다이렉트 처리 여부 판단
     */
    private boolean shouldRedirect(String redirectUrl) {
        return redirectUrl != null && !redirectUrl.isEmpty();
    }

    /**
     * 인증 쿠키 설정
     */
    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessCookie = createSecureCookie("admin_access_token", accessToken, 30 * 60); // 30분
        Cookie refreshCookie = createSecureCookie("admin_refresh_token", refreshToken, 14 * 24 * 60 * 60); // 14일

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    /**
     * 인증 쿠키 제거
     */
    private void clearAuthCookies(HttpServletResponse response) {
        Cookie accessCookie = createSecureCookie("admin_access_token", "", 0);
        Cookie refreshCookie = createSecureCookie("admin_refresh_token", "", 0);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    /**
     * 보안 쿠키 생성
     */
    private Cookie createSecureCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS 환경에서는 true로 설정
        cookie.setPath("/admin");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    /**
     * 요청에서 쿠키 값 추출
     */
    private String extractCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
