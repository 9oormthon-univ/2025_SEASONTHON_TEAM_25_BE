package com.freedom.common.exception;

import com.freedom.common.exception.custom.*;
import com.freedom.common.notification.DiscordWebhookClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordWebhookClient discordWebhookClient;

    // ============= Admin 서버에서 실제 사용하는 예외들 =============
    
    @ExceptionHandler(QuizNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleQuizNotFoundException(QuizNotFoundException e) {
        log.warn("퀴즈를 찾을 수 없음: {}", e.getMessage());
        return createErrorResponse(ErrorCode.QUIZ_NOT_FOUND);
    }

    @ExceptionHandler(NewsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNewsNotFoundException(NewsNotFoundException e) {
        log.warn("뉴스를 찾을 수 없음: {}", e.getMessage());
        return createErrorResponse(ErrorCode.NEWS_NOT_FOUND);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException e) {
        log.warn("비밀번호 불일치: {}", e.getMessage());
        return createErrorResponse(ErrorCode.INVALID_PASSWORD);
    }

    @ExceptionHandler(UserWithdrawnException.class)
    public ResponseEntity<ErrorResponse> handleUserWithdrawnException(UserWithdrawnException e) {
        log.warn("탈퇴한 사용자 접근: {}", e.getMessage());
        return createErrorResponse(ErrorCode.USER_WITHDRAWN);
    }

    @ExceptionHandler(UserSuspendedException.class)
    public ResponseEntity<ErrorResponse> handleUserSuspendedException(UserSuspendedException e) {
        log.warn("정지된 사용자 접근: {}", e.getMessage());
        return createErrorResponse(ErrorCode.USER_SUSPENDED);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException e) {
        log.warn("토큰 만료: {}", e.getMessage());
        return createErrorResponse(ErrorCode.TOKEN_EXPIRED);
    }

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handleTokenInvalidException(TokenInvalidException e) {
        log.warn("유효하지 않은 토큰: {}", e.getMessage());
        return createErrorResponse(ErrorCode.TOKEN_INVALID);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException e) {
        log.warn("이메일 중복: {}", e.getMessage());
        return createErrorResponse(ErrorCode.DUPLICATE_EMAIL);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("사용자를 찾을 수 없음: {}", e.getMessage());
        return createErrorResponse(ErrorCode.USER_NOT_FOUND);
    }

    // RefreshToken 관련 예외들 (실제 사용됨)
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpiredException(RefreshTokenExpiredException e) {
        log.warn("리프레시 토큰 만료: {}", e.getMessage());
        return createErrorResponse(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    @ExceptionHandler(RefreshTokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenInvalidException(RefreshTokenInvalidException e) {
        log.warn("유효하지 않은 리프레시 토큰: {}", e.getMessage());
        return createErrorResponse(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    // 공통 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e, HttpServletRequest request) {
        log.error("예상하지 못한 예외 발생: {} {}", request.getMethod(), request.getRequestURI(), e);
        
        // Discord 알림 전송
        try {
            discordWebhookClient.sendErrorMessage(
                "🚨 Admin Server 예외 발생",
                "**요청:** " + request.getMethod() + " " + request.getRequestURI() + 
                "\n**오류:** " + e.getMessage()
            );
        } catch (Exception discordException) {
            log.error("Discord 알림 전송 실패", discordException);
        }
        
        return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(ErrorCode errorCode) {
        ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(errorResponse);
    }
}
