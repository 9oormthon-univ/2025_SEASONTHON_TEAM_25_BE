package com.freedom.auth.domain.service;

import com.freedom.auth.domain.RefreshToken;
import com.freedom.auth.infra.RefreshTokenJpaRepository;
import com.freedom.common.exception.custom.RefreshTokenExpiredException;
import com.freedom.common.exception.custom.RefreshTokenInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    
    private final RefreshTokenJpaRepository refreshTokenRepository;
    
    public void saveRefreshToken(Long userId, String token, LocalDateTime expiresAt) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenInvalidException("토큰을 찾을 수 없음"));

        if (refreshToken.isExpired()) {
            throw new RefreshTokenExpiredException(token);
        }

        return refreshToken;
    }

    public void deleteRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenInvalidException("삭제할 토큰을 찾을 수 없음"));
        
        refreshTokenRepository.delete(refreshToken);
    }
}
