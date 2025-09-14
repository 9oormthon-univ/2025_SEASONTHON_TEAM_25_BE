package com.freedom.common.exception.custom;

public class UnsupportedQuestTypeException extends RuntimeException {
    
    public UnsupportedQuestTypeException() {
        super("지원하지 않는 퀘스트 타입입니다.");
    }
}
