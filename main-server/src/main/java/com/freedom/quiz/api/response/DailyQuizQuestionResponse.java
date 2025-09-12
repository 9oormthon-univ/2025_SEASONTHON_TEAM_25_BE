package com.freedom.quiz.api.response;

import com.freedom.quiz.application.dto.DailyQuizDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DailyQuizQuestionResponse {
    private final boolean isCompleted;
    private final List<QuizQuestionResponse> quizzes;

    public static DailyQuizQuestionResponse from(DailyQuizDto dailyQuizDto) {
        List<QuizQuestionResponse> quizResponses = dailyQuizDto.getUserQuizzes().stream()
                .map(QuizQuestionResponse::from)
                .toList();

        return DailyQuizQuestionResponse.builder()
                .isCompleted(dailyQuizDto.isCompleted())
                .quizzes(quizResponses)
                .build();
    }
}
