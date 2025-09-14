package com.freedom.common.exception.custom;

public class QuestAlreadyCompletedException extends RuntimeException {
    
    public QuestAlreadyCompletedException() {
        super("이미 완료된 퀘스트입니다.");
    }
    
    public QuestAlreadyCompletedException(String message) {
        super(message);
    }
    
    public QuestAlreadyCompletedException(Long userQuestId) {
        super("이미 완료된 퀘스트입니다. userQuestId: " + userQuestId);
    }
}
