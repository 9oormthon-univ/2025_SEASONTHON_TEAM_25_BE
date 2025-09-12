package com.freedom.quiz.application.dto;

import com.freedom.quiz.domain.entity.Quiz;
import com.freedom.quiz.domain.entity.QuizType;
import com.freedom.quiz.domain.entity.UserQuiz;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserQuizDto {
    private final Long userQuizId;
    private final Long quizId;
    private final QuizType type;
    private final String category;  // 퀴즈 카테고리 (news, quiz)
    private final String question;
    private final String explanation;
    private final String hint;
    
    // OX 타입
    private final Boolean oxAnswer;
    
    // MCQ 타입
    private final String mcqOption1;
    private final String mcqOption2;
    private final String mcqOption3;
    private final String mcqOption4;
    private final Integer mcqCorrectIndex;
    
    // 사용자 답안 정보
    private final String userAnswer;
    private final Boolean isCorrect;

    private final String newsUrl;
    
    // 퀴즈 날짜
    private final LocalDate quizDate;

    public static UserQuizDto from(UserQuiz userQuiz, Quiz quiz, String newsUrl) {
        return UserQuizDto.builder()
                .userQuizId(userQuiz.getId())
                .quizId(quiz.getId())
                .type(quiz.getType())
                .category(quiz.getCategory())
                .question(quiz.getQuestion())
                .explanation(quiz.getExplanation())
                .hint(quiz.getHint())
                .oxAnswer(quiz.getOxAnswer())
                .mcqOption1(quiz.getMcqOption1())
                .mcqOption2(quiz.getMcqOption2())
                .mcqOption3(quiz.getMcqOption3())
                .mcqOption4(quiz.getMcqOption4())
                .mcqCorrectIndex(quiz.getMcqCorrectIndex())
                .userAnswer(userQuiz.getUserAnswer())
                .isCorrect(userQuiz.getIsCorrect())
                .newsUrl(newsUrl)
                .quizDate(userQuiz.getQuizDate())
                .build();
    }

    public static UserQuizDto fromQuestionOnly(UserQuiz userQuiz, Quiz quiz, String newsUrl) {
        return UserQuizDto.builder()
                .userQuizId(userQuiz.getId())
                .quizId(quiz.getId())
                .type(quiz.getType())
                .category(quiz.getCategory())
                .question(quiz.getQuestion())
                .explanation(null)
                .hint(null)
                .oxAnswer(null)
                .mcqOption1(quiz.getMcqOption1())
                .mcqOption2(quiz.getMcqOption2())
                .mcqOption3(quiz.getMcqOption3())
                .mcqOption4(quiz.getMcqOption4())
                .mcqCorrectIndex(null)
                .userAnswer(userQuiz.getUserAnswer())
                .isCorrect(userQuiz.getIsCorrect())
                .newsUrl(newsUrl)
                .quizDate(userQuiz.getQuizDate())
                .build();
    }
}
