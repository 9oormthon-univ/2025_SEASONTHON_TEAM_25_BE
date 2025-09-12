package com.freedom.quiz.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizAnswerRequest {
    @NotNull(message = "userQuizId를 입력해주세요.")
    private Long userQuizId;
    
    @NotBlank(message = "답안을 입력해주세요.")
    private String userAnswer;
}
