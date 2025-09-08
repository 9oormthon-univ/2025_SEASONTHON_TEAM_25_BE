package com.freedom.quiz.domain.dto;

import com.freedom.quiz.domain.entity.Quiz;
import com.freedom.quiz.domain.entity.QuizDifficulty;
import com.freedom.quiz.domain.entity.QuizType;
import lombok.Builder;

/**
 * 도메인 계층에서 사용하는 Quiz DTO
 * Entity의 상세 정보를 Application 계층으로 전달하지 않기 위한 변환 객체
 */
@Builder
public record QuizDomainDto(
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
    
    public static QuizDomainDto from(Quiz quiz) {
        return QuizDomainDto.builder()
                .id(quiz.getId())
                .type(quiz.getType())
                .difficulty(quiz.getDifficulty())
                .category(quiz.getCategory())
                .newsArticleId(quiz.getNewsArticleId())
                .question(quiz.getQuestion())
                .explanation(quiz.getExplanation())
                .oxAnswer(quiz.getOxAnswer())
                .mcqOption1(quiz.getMcqOption1())
                .mcqOption2(quiz.getMcqOption2())
                .mcqOption3(quiz.getMcqOption3())
                .mcqOption4(quiz.getMcqOption4())
                .mcqCorrectIndex(quiz.getMcqCorrectIndex())
                .build();
    }
}
