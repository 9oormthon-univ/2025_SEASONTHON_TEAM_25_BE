package com.freedom.quiz.domain.entity;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_quiz")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserQuiz extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "is_correct")
    private Boolean isCorrect;  // null: 미풀이, true: 정답, false: 오답

    @Column(name = "user_answer")
    private String userAnswer;  // 사용자가 선택한 답

    @Column(name = "quiz_date", nullable = false)
    private LocalDate quizDate;  // 퀴즈가 출제된 날짜

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;  // 퀴즈가 배정된 날짜 (현재는 quiz_date와 동일하게 설정)

    // 답안 제출 시 업데이트
    public void submitAnswer(String userAnswer, boolean isCorrect) {
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
    }

    // 생성 시 assignedDate 자동 설정
    @PrePersist
    protected void onCreate() {
        if (assignedDate == null) {
            assignedDate = quizDate; // quiz_date와 동일하게 설정
        }
    }
}
