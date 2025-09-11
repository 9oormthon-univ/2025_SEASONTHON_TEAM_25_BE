package com.freedom.quiz.api.response;

import com.freedom.quiz.application.dto.QuizDomainDto;
import com.freedom.quiz.domain.entity.QuizType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminQuizResponse {
    private Long id;
    private QuizType type;
    private String category;
    private String question;
    private Long newsArticleId;

    public static AdminQuizResponse from(QuizDomainDto dto) {
        return AdminQuizResponse.builder()
                .id(dto.id())
                .type(dto.type())
                .category(dto.category())
                .question(dto.question())
                .newsArticleId(dto.newsArticleId())
                .build();
    }
}
