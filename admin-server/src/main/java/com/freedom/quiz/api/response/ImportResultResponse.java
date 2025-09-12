package com.freedom.quiz.api.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImportResultResponse {
    private int importedCount;
    private String message;

    public static ImportResultResponse from(int importedCount) {
        return ImportResultResponse.builder()
                .importedCount(importedCount)
                .message(String.format("외부 퀴즈 %d개를 성공적으로 가져왔습니다.", importedCount))
                .build();
    }
}
