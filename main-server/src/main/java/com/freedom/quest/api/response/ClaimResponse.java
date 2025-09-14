package com.freedom.quest.api.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClaimResponse {
    private boolean completed;
    private Long userQuestId;
    private String message;

    public static ClaimResponse success(Long userQuestId) {
        return ClaimResponse.builder()
                .completed(true)
                .userQuestId(userQuestId)
                .message("퀘스트 보상이 성공적으로 지급되었습니다.")
                .build();
    }

    public static ClaimResponse alreadyClaimed(Long userQuestId) {
        return ClaimResponse.builder()
                .completed(false)
                .userQuestId(userQuestId)
                .message("이미 보상을 받았습니다.")
                .build();
    }
}
