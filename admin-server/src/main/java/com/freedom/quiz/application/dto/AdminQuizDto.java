package com.freedom.quiz.application.dto;

import com.freedom.quiz.domain.dto.QuizDomainDto;
import com.freedom.quiz.domain.entity.QuizDifficulty;
import com.freedom.quiz.domain.entity.QuizType;
import lombok.Builder;

/**
 * Application 계층에서 사용하는 Quiz DTO
 * 도메인 DTO를 받아서 필요한 비즈니스 로직 처리 후 API 계층으로 전달
 */
@Builder
public record AdminQuizDto(
        Long id,
        QuizType type,
        QuizDifficulty difficulty,
        String category,
        Long newsArticleId,
        String question,
        String explanation,
        Boolean oxAnswer,
        String mcqOption1,
        String mcqOption2,
        String mcqOption3,
        String mcqOption4,
        Integer mcqCorrectIndex
) {
    
    public static AdminQuizDto from(QuizDomainDto domainDto) {
        return AdminQuizDto.builder()
                .id(domainDto.id())
                .type(domainDto.type())
                .difficulty(domainDto.difficulty())
                .category(domainDto.category())
                .newsArticleId(domainDto.newsArticleId())
                .question(domainDto.question())
                .explanation(domainDto.explanation())
                .oxAnswer(domainDto.oxAnswer())
                .mcqOption1(domainDto.mcqOption1())
                .mcqOption2(domainDto.mcqOption2())
                .mcqOption3(domainDto.mcqOption3())
                .mcqOption4(domainDto.mcqOption4())
                .mcqCorrectIndex(domainDto.mcqCorrectIndex())
                .build();
    }
}
