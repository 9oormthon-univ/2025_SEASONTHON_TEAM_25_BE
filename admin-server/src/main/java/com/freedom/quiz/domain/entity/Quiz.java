package com.freedom.quiz.domain.entity;

import com.freedom.common.entity.BaseEntity;
import com.freedom.news.infra.client.NewsQuizGenerationClient;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "quiz")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Quiz extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10, nullable = false)
    private QuizType type; // OX, MCQ

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 10, nullable = false)
    private QuizDifficulty difficulty; // 기본 MEDIUM(중하)

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "news_article_id")
    private Long newsArticleId;

    @Column(name = "question", length = 500, nullable = false)
    private String question;

    @Column(name = "explanation", length = 500)
    private String explanation;

    @Column(name = "hint", length = 500)
    private String hint;

    // OX
    @Column(name = "ox_answer")
    private Boolean oxAnswer;

    // MCQ (보기 4개, 정답 1개)
    @Column(name = "mcq_opt1", length = 300)
    private String mcqOption1;
    @Column(name = "mcq_opt2", length = 300)
    private String mcqOption2;
    @Column(name = "mcq_opt3", length = 300)
    private String mcqOption3;
    @Column(name = "mcq_opt4", length = 300)
    private String mcqOption4;

    @Column(name = "mcq_correct_index")
    private Integer mcqCorrectIndex;

    public static Quiz createOxQuizEntity(Long newsArticleId, String category, NewsQuizGenerationClient.OxQuiz ox) {
        return Quiz.builder()
                .type(QuizType.OX)
                .difficulty(QuizDifficulty.MEDIUM)
                .category(category)
                .newsArticleId(newsArticleId)
                .question(ox.question().trim())
                .explanation(ox.explanation().trim())
                .hint(String.valueOf(newsArticleId))
                .oxAnswer(ox.answer())
                .build();
    }

    public static Quiz createMcqQuizEntity(Long newsArticleId, String category, NewsQuizGenerationClient.McqQuiz mcq) {
        List<NewsQuizGenerationClient.McqOption> options = new ArrayList<>(mcq.options());
        Collections.shuffle(options, ThreadLocalRandom.current());

        // 정답 위치 찾기
        int correctCount = 0;
        int correctIdx1Based = 0;
        for (int i = 0; i < 4; i++) {
            if (Boolean.TRUE.equals(options.get(i).correct())) {
                correctCount++;
                correctIdx1Based = i + 1;
            }
        }
        // 정답이 정확히 1개일 때만 퀴즈 생성
        if (correctCount != 1) {
            return null;
        }
        return Quiz.builder()
                .type(QuizType.MCQ)
                .difficulty(QuizDifficulty.MEDIUM)
                .category(category)
                .newsArticleId(newsArticleId)
                .question(mcq.question().trim())
                .explanation(mcq.explanation().trim())
                .hint(String.valueOf(newsArticleId))
                .mcqOption1(options.get(0).text())
                .mcqOption2(options.get(1).text())
                .mcqOption3(options.get(2).text())
                .mcqOption4(options.get(3).text())
                .mcqCorrectIndex(correctIdx1Based)
                .build();
    }
}



