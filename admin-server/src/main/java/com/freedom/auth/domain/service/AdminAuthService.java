package com.freedom.auth.domain.service;

import com.freedom.auth.application.dto.LoginDto;
import com.freedom.auth.application.dto.TokenDto;
import com.freedom.auth.domain.RefreshToken;
import com.freedom.auth.domain.User;
import com.freedom.common.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    
    private final FindUserService findUserService;
    private final ValidateUserService validateUserService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    
    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public TokenDto login(String email, String password) {
        User user = findUserService.findByEmail(email);
        validateUserService.validatePassword(password, user.getPassword());
        validateUserService.validateUserStatus(user);
        
        return createTokenResponse(user);
    }
    
    @Transactional
    public TokenDto refreshTokens(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue);
        User user = findUserService.findById(refreshToken.getUserId());
        validateUserService.validateUserStatus(user);
        refreshTokenService.deleteRefreshToken(refreshTokenValue);
        
        return createTokenResponse(user);
    }
    
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenService.deleteRefreshToken(refreshTokenValue);
    }

    private TokenDto createTokenResponse(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshTokenValue = jwtProvider.createRefreshToken(user.getId());
        
        saveRefreshTokenToDatabase(user.getId(), refreshTokenValue);
        
        LoginDto loginDto = LoginDto.from(user);
        return TokenDto.of(
                accessToken, 
                refreshTokenValue, 
                accessTokenExpiration / 1000, 
                loginDto
        );
    }
    
    private void saveRefreshTokenToDatabase(Long userId, String refreshTokenValue) {
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(refreshTokenExpiration / 1000);
        
        refreshTokenService.saveRefreshToken(userId, refreshTokenValue, expiresAt);
    }
}
