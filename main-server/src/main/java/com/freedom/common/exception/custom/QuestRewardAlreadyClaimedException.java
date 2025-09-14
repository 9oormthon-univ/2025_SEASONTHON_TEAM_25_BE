package com.freedom.common.exception.custom;

public class QuestRewardAlreadyClaimedException extends RuntimeException {
    
    public QuestRewardAlreadyClaimedException() {
        super("이미 보상을 수령한 퀘스트입니다.");
    }
    
    public QuestRewardAlreadyClaimedException(String message) {
        super(message);
    }
    
    public QuestRewardAlreadyClaimedException(Long userQuestId) {
        super("이미 보상을 수령한 퀘스트입니다. userQuestId: " + userQuestId);
    }
}
