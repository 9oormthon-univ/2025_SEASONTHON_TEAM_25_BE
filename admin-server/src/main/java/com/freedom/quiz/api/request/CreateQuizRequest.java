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

    public void validateOxQuiz() {
        if (type == QuizType.OX && oxAnswer == null) {
            throw new IllegalArgumentException("OX 퀴즈의 경우 정답이 필수입니다");
        }
    }

    public void validateMcqQuiz() {
        if (type == QuizType.MCQ) {
            if (mcqOption1 == null || mcqOption1.trim().isEmpty() ||
                mcqOption2 == null || mcqOption2.trim().isEmpty() ||
                mcqOption3 == null || mcqOption3.trim().isEmpty() ||
                mcqOption4 == null || mcqOption4.trim().isEmpty()) {
                throw new IllegalArgumentException("4지선다 퀴즈의 경우 모든 선택지가 필수입니다");
            }
            if (mcqCorrectIndex == null || mcqCorrectIndex < 1 || mcqCorrectIndex > 4) {
                throw new IllegalArgumentException("정답은 1~4 사이의 값이어야 합니다");
            }
        }
    }

    public String getCategoryDisplayName() {
        if ("NEWS_ARTICLE".equals(category)) {
            return "news";
        } else if ("ADMIN_CREATED".equals(category)) {
            return "quiz";
        }
        return category;
    }
}
