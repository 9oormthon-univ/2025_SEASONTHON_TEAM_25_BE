package com.freedom.quiz.domain.dto;

import com.freedom.quiz.domain.entity.QuizDifficulty;
import com.freedom.quiz.domain.entity.QuizType;
import lombok.Builder;

/**
 * 퀴즈 생성을 위한 도메인 커맨드 객체
 * Application 계층에서 도메인 계층으로 퀴즈 생성 정보를 전달
 */
@Builder
public record CreateQuizCommand(
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
}
