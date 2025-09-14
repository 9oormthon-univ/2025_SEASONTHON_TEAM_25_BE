package com.freedom.quiz.domain.entity;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "quiz_history")
public class QuizHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name= "quiz_id", nullable = false)
    private Long quizId;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @Builder
    public QuizHistory(Long userId, Long quizId, LocalDateTime answeredAt) {
        this.userId = userId;
        this.quizId = quizId;
        this.answeredAt = answeredAt;
    }
}
