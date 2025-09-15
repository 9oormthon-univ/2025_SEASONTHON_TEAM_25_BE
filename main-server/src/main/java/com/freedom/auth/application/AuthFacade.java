package com.freedom.auth.application;

import com.freedom.achievement.application.dto.AchievementDto;
import com.freedom.achievement.domain.entity.Achievement;
import com.freedom.achievement.domain.service.AchievementCommandService;
import com.freedom.auth.api.response.CharacterNameResponse;
import com.freedom.auth.application.dto.LoginDto;
import com.freedom.auth.application.dto.SignUpDto;
import com.freedom.auth.application.dto.TokenDto;
import com.freedom.auth.domain.RefreshToken;
import com.freedom.auth.domain.User;
import com.freedom.auth.domain.service.*;
import com.freedom.common.logging.Loggable;
import com.freedom.common.security.JwtProvider;
import com.freedom.wallet.application.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthFacade {
    
    private final SignUpUserService signUpUserService;
    private final FindUserService findUserService;
    private final ValidateUserService validateUserService;
    private final RefreshTokenService refreshTokenService;
    private final CharacterNameService characterNameService;
    private final AchievementCommandService achievementService;
    private final WalletService walletService;
    private final JwtProvider jwtProvider;
    
    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Loggable("회원가입 처리")
    @Transactional
    public SignUpDto signUp(String email, String password) {
        validateUserService.validateEmailDuplication(email);
        
        User user = signUpUserService.signUp(email, password);
        return SignUpDto.from(user);
    }

    @Loggable("로그인 처리")
    @Transactional
    public TokenDto login(String email, String password) {
        User user = findUserService.findByEmail(email);
        validateUserService.validatePassword(password, user.getPassword());
        validateUserService.validateUserStatus(user);
        
        return createTokenResponse(user);
    }
    
    @Loggable("토큰 갱신 처리")
    @Transactional
    public TokenDto refreshTokens(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue);
        User user = findUserService.findById(refreshToken.getUserId());
        validateUserService.validateUserStatus(user);
        refreshTokenService.deleteRefreshToken(refreshTokenValue);
        
        return createTokenResponse(user);
    }
    
    @Loggable("로그아웃 처리")
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenService.deleteRefreshToken(refreshTokenValue);
    }

    @Loggable("캐릭터 이름 생성 처리")
    @Transactional
    public CharacterNameResponse createCharacterName(Long userId, String characterName) {
        characterName = characterNameService.createCharacterName(userId, characterName);
        walletService.createWallet(userId);
        AchievementDto achievementDto = achievementService.grantAchievement(userId, Achievement.AchievementType.BEGINNERS_LUCK);
        return CharacterNameResponse.success(characterName, achievementDto);
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
