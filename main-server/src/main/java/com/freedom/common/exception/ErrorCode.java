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
    API_NOT_FOUND("COMMON003", "요청하신 API 경로를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    
    // Validation 에러
    VALIDATION_ERROR("VALIDATION001", "", HttpStatus.BAD_REQUEST),
    
    // 인증/인가 에러
    INVALID_CREDENTIALS("AUTH001", "잘못된 인증 정보입니다.", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("AUTH002", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH003", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("AUTH004", "리프레시 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID("AUTH005", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("AUTH006", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    
    // 사용자 에러
    USER_NOT_FOUND("USER001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL("USER002", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_PASSWORD("USER003", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    USER_WITHDRAWN("USER004", "탈퇴한 사용자입니다.", HttpStatus.FORBIDDEN),
    USER_SUSPENDED("USER005", "정지된 사용자입니다.", HttpStatus.FORBIDDEN),

    // 상품/조회 에러
    PRODUCT_NOT_FOUND("PRODUCT001","상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    
    // 뉴스 에러
    NEWS_NOT_FOUND("NEWS001", "뉴스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    
    // 퀴즈 에러
    QUIZ_NOT_FOUND("QUIZ001", "퀴즈를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    QUIZ_VALIDATION_ERROR("QUIZ002", "퀴즈 데이터가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    USER_QUIZ_NOT_FOUND("QUIZ003", "사용자 퀴즈를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_QUIZ_ANSWER("QUIZ004", "유효하지 않은 퀴즈 답안입니다.", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_QUIZ("QUIZ005", "출제 가능한 퀴즈가 부족합니다.", HttpStatus.BAD_REQUEST),
    INVALID_MCQ_OPTION("QUIZ006", "유효하지 않은 선택지입니다.", HttpStatus.BAD_REQUEST),
    QUIZ_ALREADY_SUBMITTED("QUIZ007", "이미 답안이 제출된 퀴즈입니다.", HttpStatus.BAD_REQUEST),

    // 캐릭터 생성 에러
    CHARACTER_ALREADY_CREATED("CHAR001", "이미 캐릭터가 생성되었습니다.", HttpStatus.CONFLICT),
    CHARACTER_NAME_INVALID   ("CHAR002", "캐릭터 이름이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    CHARACTER_NAME_DUPLICATE ("CHAR003", "이미 사용 중인 캐릭터 이름입니다.", HttpStatus.BAD_REQUEST),

    // 적금 에러
    SAVING_SUBSCRIPTION_NOT_FOUND("SAV001", "적금 구독을 찾을 수 없거나 권한이 없습니다.", HttpStatus.NOT_FOUND),
    SAVING_SUBSCRIPTION_INVALID_STATE("SAV002", "해당 상태에선 수행할 수 없습니다.", HttpStatus.BAD_REQUEST),
    SAVING_NO_NEXT_PLANNED_PAYMENT("SAV003", "다음 납입 계획이 없습니다.", HttpStatus.BAD_REQUEST),
    SAVING_INVALID_PAYMENT_AMOUNT("SAV004", "납입 금액이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    SAVING_INVALID_DATES("SAV005", "시작일/만기일이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    SAVING_PAYMENT_INVALID_PARAMS("SAV006", "납입 파라미터가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    SAVING_POLICY_INVALID("SAV007", "정책 파라미터가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    SAVING_SNAPSHOT_IDENTIFIERS_INVALID("SAV008", "스냅샷 식별자가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    SAVING_DUPLICATE_SUBSCRIPTION("SAV009", "이미 가입한 적금에 또 가입할 수 없습니다.", HttpStatus.BAD_REQUEST),
    SAVING_NO_TODAY_PAYABLE("SAV010", "오늘 납입 가능한 회차가 없습니다.", HttpStatus.BAD_REQUEST),

    // 퀘스트 에러
    USER_QUEST_NOT_FOUND("QUEST001", "사용자 퀘스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_WALLET_NOT_FOUND("QUEST002", "사용자 지갑을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    QUEST_ALREADY_COMPLETED("QUEST003", "이미 완료된 퀘스트입니다.", HttpStatus.BAD_REQUEST),
    QUEST_REWARD_ALREADY_CLAIMED("QUEST004", "이미 보상을 수령한 퀘스트입니다.", HttpStatus.BAD_REQUEST),
    QUEST_ACCESS_DENIED("QUEST005", "해당 퀘스트에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    UNSUPPORTED_QUEST_TYPE("QUEST006", "지원하지 않는 퀘스트 타입입니다.", HttpStatus.BAD_REQUEST),

    // 스크랩 에러
    SCRAP_ALREADY_EXISTS("SCRAP001", "이미 스크랩한 뉴스입니다.", HttpStatus.CONFLICT),
    
    // 출석 에러
    ATTENDANCE_INVALID_YEAR("ATTENDANCE001", "년도는 2000~2100 사이의 값이어야 합니다.", HttpStatus.BAD_REQUEST),
    ATTENDANCE_INVALID_MONTH("ATTENDANCE002", "월은 1~12 사이의 값이어야 합니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
