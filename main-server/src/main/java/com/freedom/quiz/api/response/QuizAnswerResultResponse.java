package com.freedom.quiz.api.response;

import com.freedom.quiz.application.dto.UserQuizDto;
import com.freedom.quiz.domain.entity.Quiz;
import com.freedom.quiz.domain.entity.QuizType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizAnswerResultResponse {
    private final boolean isCorrect;
    private final String explanation;
    private final String hint;
    private final String correctAnswer;


    public static QuizAnswerResultResponse from(boolean isCorrect, UserQuizDto quiz) {
        String correctAnswer = quiz.getType() == QuizType.OX
                ?  String.valueOf(quiz.getOxAnswer())
                :  String.valueOf(quiz.getMcqCorrectIndex());

        return QuizAnswerResultResponse.builder()
                .isCorrect(isCorrect)
                .explanation(quiz.getExplanation())
                .hint(quiz.getHint())
                .correctAnswer(correctAnswer)
                .build();
    }
}
