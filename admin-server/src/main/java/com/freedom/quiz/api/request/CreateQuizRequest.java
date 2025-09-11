package com.freedom.quiz.api.request;

import com.freedom.quiz.domain.entity.QuizDifficulty;
import com.freedom.quiz.domain.entity.QuizType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizRequest {

    @NotNull(message = "퀴즈 타입은 필수입니다")
    private QuizType type;

    @NotNull(message = "퀴즈 난이도는 필수입니다")
    private QuizDifficulty difficulty;

    @NotBlank(message = "카테고리는 필수입니다")
    @Size(max = 50, message = "카테고리는 50자를 초과할 수 없습니다")
    private String category;

    private Long newsArticleId;

    @NotBlank(message = "문제는 필수입니다")
    @Size(max = 500, message = "문제는 500자를 초과할 수 없습니다")
    private String question;

    @Size(max = 500, message = "해설은 500자를 초과할 수 없습니다")
    private String explanation;

    @Size(max = 500, message = "힌트는 500자를 초과할 수 없습니다")
    private String hint;

    // OX 타입용 필드
    private Boolean oxAnswer;
    // MCQ 타입용 필드
    @Size(max = 300, message = "선택지는 300자를 초과할 수 없습니다")
    private String mcqOption1;

    @Size(max = 300, message = "선택지는 300자를 초과할 수 없습니다")
    private String mcqOption2;

    @Size(max = 300, message = "선택지는 300자를 초과할 수 없습니다")
    private String mcqOption3;

    @Size(max = 300, message = "선택지는 300자를 초과할 수 없습니다")
    private String mcqOption4;

    private Integer mcqCorrectIndex;
}
