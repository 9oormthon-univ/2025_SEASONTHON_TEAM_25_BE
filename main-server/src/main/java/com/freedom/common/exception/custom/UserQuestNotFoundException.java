package com.freedom.common.exception.custom;

public class UserQuestNotFoundException extends RuntimeException {
    
    public UserQuestNotFoundException() {
        super("사용자 퀘스트를 찾을 수 없습니다.");
    }
    
    public UserQuestNotFoundException(String message) {
        super(message);
    }
    
    public UserQuestNotFoundException(Long userQuestId) {
        super("사용자 퀘스트를 찾을 수 없습니다. userQuestId: " + userQuestId);
    }
}
