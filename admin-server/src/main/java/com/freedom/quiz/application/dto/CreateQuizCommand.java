package com.freedom.quiz.application.dto;

import com.freedom.quiz.domain.entity.QuizDifficulty;
import com.freedom.quiz.domain.entity.QuizType;
import lombok.Builder;

@Builder
public record CreateQuizCommand(
        QuizType type,
        QuizDifficulty difficulty,
        String category,
        Long newsArticleId,
        String question,
        String explanation,
        String hint,
        Boolean oxAnswer,
        String mcqOption1,
        String mcqOption2,
        String mcqOption3,
        String mcqOption4,
        Integer mcqCorrectIndex
) {
}
