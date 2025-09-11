package com.freedom.common.security;

import com.freedom.common.exception.custom.TokenExpiredException;
import com.freedom.common.exception.custom.TokenInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {
    
    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    
    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token.expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token.expiration}") long refreshTokenExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
    
    public String createAccessToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }
    
    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("type", String.class);
    }
    
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (TokenExpiredException | TokenInvalidException | JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 토큰 상태를 세밀하게 검증하는 메서드
     */
    public TokenValidationResult validateAccessToken(String token) {
        try {
            parseClaims(token);
            
            // 토큰 타입 확인
            if (!"ACCESS".equals(getTokenType(token))) {
                return TokenValidationResult.wrongType();
            }
            
            return TokenValidationResult.valid();
            
        } catch (TokenExpiredException e) {
            return TokenValidationResult.expired();
        } catch (TokenInvalidException | JwtException | IllegalArgumentException e) {
            return TokenValidationResult.invalid(e.getMessage());
        }
    }
    
    public boolean isAccessToken(String token) {
        try {
            return "ACCESS".equals(getTokenType(token));
        } catch (TokenExpiredException | TokenInvalidException | JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public boolean isRefreshToken(String token) {
        try {
            return "REFRESH".equals(getTokenType(token));
        } catch (TokenExpiredException | TokenInvalidException | JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("JWT");
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidException(e.getMessage());
        }
    }
}
