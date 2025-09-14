package com.freedom.common.exception.custom;

public class QuestAccessDeniedException extends RuntimeException {
    
    public QuestAccessDeniedException() {
        super("해당 퀘스트에 접근할 권한이 없습니다.");
    }
    
    public QuestAccessDeniedException(String message) {
        super(message);
    }
    
    public QuestAccessDeniedException(Long userQuestId) {
        super("해당 퀘스트에 접근할 권한이 없습니다. userQuestId: " + userQuestId);
    }
}
