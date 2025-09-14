package com.freedom.quiz.domain.service;

import com.freedom.quiz.domain.entity.QuizHistory;
import com.freedom.quiz.infra.QuizHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuizHistorySaveService {

    private final QuizHistoryRepository quizHistoryRepository;

    public void quizHistorySave(Long userId, Long quizId) {
        QuizHistory history = quizHistoryRepository.findByUserIdAndQuizId(userId, quizId);
        if(history == null) {
            QuizHistory quizHistory = QuizHistory.builder()
                    .userId(userId)
                    .quizId(quizId)
                    .answeredAt(LocalDateTime.now())
                    .build();
            quizHistoryRepository.save(quizHistory);
        }
    }
}
