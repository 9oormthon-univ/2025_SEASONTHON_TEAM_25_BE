package com.freedom.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // 공통 에러
    INTERNAL_SERVER_ERROR("COMMON001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    METHOD_NOT_ALLOWED("COMMON002", "허용되지 않은 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    
    // Validation 에러
    VALIDATION_ERROR("VALIDATION001", "", HttpStatus.BAD_REQUEST),
    
    // 인증/인가 에러 (Admin 서버에서 사용)
    INVALID_CREDENTIALS("AUTH001", "잘못된 인증 정보입니다.", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("AUTH002", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH003", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("AUTH004", "리프레시 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID("AUTH005", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("AUTH006", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    
    // 사용자 에러 (Admin 서버에서 사용)
    USER_NOT_FOUND("USER001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL("USER002", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_PASSWORD("USER003", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    USER_WITHDRAWN("USER004", "탈퇴한 사용자입니다.", HttpStatus.FORBIDDEN),
    USER_SUSPENDED("USER005", "정지된 사용자입니다.", HttpStatus.FORBIDDEN),
    
    // 뉴스 에러 (Admin 서버에서 사용)
    NEWS_NOT_FOUND("NEWS001", "뉴스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    
    // 퀴즈 에러 (Admin 서버에서 사용)
    QUIZ_NOT_FOUND("QUIZ001", "퀴즈를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    QUIZ_VALIDATION_ERROR("QUIZ002", "퀴즈 데이터가 유효하지 않습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
