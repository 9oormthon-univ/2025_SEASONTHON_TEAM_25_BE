package com.freedom.auth.api;

import com.freedom.auth.api.dto.AdminAuthCheckResponse;
import com.freedom.auth.api.dto.AdminLoginRequest;
import com.freedom.auth.api.dto.AdminLoginResponse;
import com.freedom.auth.api.dto.AdminLogoutResponse;
import com.freedom.auth.application.AdminAuthFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 인증 API 컨트롤러
 * - HTTP 요청/응답 처리에만 집중
 * - 비즈니스 로직은 Facade에 위임
 */
@RestController
@RequestMapping("/admin/api/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthFacade adminAuthFacade;

    /**
     * 관리자 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request, 
                                                   HttpServletResponse response) {
        return adminAuthFacade.handleLogin(request.getEmail(), request.getPassword(), response);
    }

    /**
     * 관리자 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<AdminLogoutResponse> logout(HttpServletResponse response) {
        return adminAuthFacade.handleLogout(response);
    }

    /**
     * 토큰 갱신
     */
    @RequestMapping(value = "/refresh", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<AdminLoginResponse> refresh(HttpServletRequest request, 
                                                     HttpServletResponse response) {
        return adminAuthFacade.handleTokenRefresh(request, response);
    }

    /**
     * 인증 상태 확인
     */
    @GetMapping("/check")
    public ResponseEntity<AdminAuthCheckResponse> check() {
        return ResponseEntity.ok(AdminAuthCheckResponse.authenticated());
    }
}
