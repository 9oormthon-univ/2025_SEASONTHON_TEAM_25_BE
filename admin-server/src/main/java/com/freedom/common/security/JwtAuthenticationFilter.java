package com.freedom.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터 - 단순화된 토큰 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        String token = resolveToken(request);
        
        try {
            if (StringUtils.hasText(token)) {
                TokenValidationResult validationResult = jwtProvider.validateAccessToken(token);
                
                if (validationResult.isValid()) {
                    // 유효한 토큰인 경우 인증 설정
                    setAuthentication(token);
                } else {
                    // 토큰에 문제가 있는 경우 로그 기록 및 인증 정보 제거
                    logTokenValidationFailure(request, validationResult);
                    SecurityContextHolder.clearContext();
                    
                    // 토큰 만료의 경우 request에 속성 설정 (AuthenticationEntryPoint에서 사용)
                    if (validationResult.isExpired()) {
                        request.setAttribute("tokenExpired", true);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("JWT 토큰 처리 중 예상치 못한 오류 발생: {} - {}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void logTokenValidationFailure(HttpServletRequest request, TokenValidationResult validationResult) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        switch (validationResult.getStatus()) {
            case EXPIRED -> log.warn("만료된 JWT 토큰으로 접근: {} {}", method, uri);
            case INVALID -> log.warn("유효하지 않은 JWT 토큰으로 접근: {} {} - {}", method, uri, validationResult.getMessage());
            case WRONG_TYPE -> log.warn("잘못된 토큰 타입으로 접근: {} {}", method, uri);
        }
    }
    
    /**
     * JWT 토큰으로부터 인증 정보 설정
     */
    private void setAuthentication(String token) {
        Long userId = jwtProvider.getUserId(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, 
                null, 
                userDetails.getAuthorities()
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    /**
     * 요청 헤더 또는 쿠키에서 JWT 토큰 추출
     */
    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 토큰 추출 (API 요청용)
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        // 2. 쿠키에서 토큰 추출 (관리자 웹 페이지용)
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("admin_access_token".equals(cookie.getName())) {
                    String cookieToken = cookie.getValue();
                    if (StringUtils.hasText(cookieToken)) {
                        log.debug("쿠키에서 JWT 토큰 추출: {}", request.getRequestURI());
                        return cookieToken;
                    }
                }
            }
        }
        
        return null;
    }
}
