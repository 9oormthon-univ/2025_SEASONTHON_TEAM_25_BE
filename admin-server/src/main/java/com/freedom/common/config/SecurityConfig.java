package com.freedom.common.config;

import com.freedom.common.security.JwtAuthenticationEntryPoint;
import com.freedom.common.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용으로 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정 (필요시 추가)
                .cors(AbstractHttpConfigurer::disable)
                // 세션 사용하지 않음
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URL별 인가 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 불필요한 엔드포인트
                        .dispatcherTypeMatchers(
                                DispatcherType.FORWARD,
                                DispatcherType.ERROR,
                                DispatcherType.INCLUDE
                        ).permitAll()
                        // ★ 에러/파비콘/정적 리소스 허용
                        .requestMatchers("/error", "/favicon.ico").permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/assets/**","/css/**","/js/**","/images/**","/webjars/**","/static/**").permitAll()

                        // ★ 공개 엔드포인트
                        .requestMatchers(
                                "/login",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/WEB-INF/views/**"
                        ).permitAll()
                        
                        // ★ nginx 프록시 경로 (admin 접두어 포함)
                        .requestMatchers(
                                "/admin/login",
                                "/admin/api/auth/login",
                                "/admin/api/auth/refresh",
                                "/admin/css/**",
                                "/admin/js/**",
                                "/admin/images/**"
                        ).permitAll()

                        // ★ 관리자 보호
                        .requestMatchers("/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // 인증 실패 시 예외 처리
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
