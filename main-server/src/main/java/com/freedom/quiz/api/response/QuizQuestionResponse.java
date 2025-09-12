package com.freedom.quiz.api.response;

import com.freedom.quiz.application.dto.UserQuizDto;
import com.freedom.quiz.domain.entity.QuizType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuizQuestionResponse {
    private final Long userQuizId;
    private final Long quizId;
    private final QuizType type;
    private final String question;
    private final List<String> mcqOptions;

    public static QuizQuestionResponse from(UserQuizDto userQuizDto) {
        List<String> mcqOptions = null;
        if (userQuizDto.getType() == QuizType.MCQ) {
            mcqOptions = List.of(
                    userQuizDto.getMcqOption1(),
                    userQuizDto.getMcqOption2(),
                    userQuizDto.getMcqOption3(),
                    userQuizDto.getMcqOption4()
            );
        }

        return QuizQuestionResponse.builder()
                .userQuizId(userQuizDto.getUserQuizId())
                .quizId(userQuizDto.getQuizId())
                .type(userQuizDto.getType())
                .question(userQuizDto.getQuestion())
                .mcqOptions(mcqOptions)
                .build();
    }
}
