package com.freedom.quiz.domain.entity;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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
    private String category; // 예: "뉴스 기사"

    @Column(name = "news_article_id")
    private Long newsArticleId; // 원천 뉴스(id)

    @Column(name = "question", length = 500, nullable = false)
    private String question;

    @Column(name = "explanation", length = 500)
    private String explanation;

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
    private Integer mcqCorrectIndex; // 1..4

    /**
     * 사용자 답안이 정답인지 확인
     */
    public boolean isCorrectAnswer(String userAnswer) {
        if (type == QuizType.OX) {
            return oxAnswer != null && oxAnswer.toString().equalsIgnoreCase(userAnswer);
        } else if (type == QuizType.MCQ) {
            return mcqCorrectIndex != null && mcqCorrectIndex.toString().equals(userAnswer);
        }
        return false;
    }
}



