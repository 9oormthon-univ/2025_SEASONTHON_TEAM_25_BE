package com.freedom.quiz.infra.client.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExternalQuizApiResponse {
    
    private int currentCount;
    private List<ExternalQuizItem> data;
    private int matchCount;
    private int page;
    private int perPage;
    private int totalCount;

    public static ExternalQuizApiResponse empty() {
        return ExternalQuizApiResponse.builder()
                .currentCount(0)
                .data(List.of())
                .matchCount(0)
                .page(1)
                .perPage(0)
                .totalCount(0)
                .build();
    }
}
