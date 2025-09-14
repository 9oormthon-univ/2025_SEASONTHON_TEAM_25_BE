package com.freedom.quiz.infra;

import com.freedom.quiz.domain.entity.QuizHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface QuizHistoryRepository extends JpaRepository<QuizHistory, Long> {

    QuizHistory findByUserIdAndQuizId(Long userId, Long quizId);

    int countByUserIdAndAnsweredAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
