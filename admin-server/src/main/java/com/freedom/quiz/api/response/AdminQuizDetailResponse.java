package com.freedom.quiz.api.response;

import com.freedom.quiz.application.dto.QuizDomainDto;
import com.freedom.quiz.domain.entity.QuizType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminQuizDetailResponse {
    private Long id;
    private QuizType type;
    private String category;
    private String question;
    private String explanation;
    private String hint;
    private Boolean oxAnswer;
    private String mcqOption1;
    private String mcqOption2;
    private String mcqOption3;
    private String mcqOption4;
    private Integer mcqCorrectIndex;
    private Long newsArticleId;

    public static AdminQuizDetailResponse from(QuizDomainDto dto) {
        return AdminQuizDetailResponse.builder()
                .id(dto.id())
                .type(dto.type())
                .category(dto.category())
                .question(dto.question())
                .explanation(dto.explanation())
                .hint(dto.hint())
                .oxAnswer(dto.oxAnswer())
                .mcqOption1(dto.mcqOption1())
                .mcqOption2(dto.mcqOption2())
                .mcqOption3(dto.mcqOption3())
                .mcqOption4(dto.mcqOption4())
                .mcqCorrectIndex(dto.mcqCorrectIndex())
                .newsArticleId(dto.newsArticleId())
                .build();
    }
}
