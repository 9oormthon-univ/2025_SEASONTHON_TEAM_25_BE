package com.freedom.quiz.domain.service;

import com.freedom.common.exception.custom.UserQuizNotFoundException;
import com.freedom.quiz.application.dto.UserQuizDto;
import com.freedom.quiz.domain.entity.UserQuiz;
import com.freedom.quiz.infra.QuizHistoryRepository;
import com.freedom.quiz.infra.UserQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FindUserQuizService {

    private final UserQuizRepository userQuizRepository;
    private final QuizHistoryRepository quizHistoryRepository;

    @Transactional(readOnly = true)
    public List<UserQuizDto> findDailyQuizzes(Long userId, LocalDate quizDate) {
        List<UserQuiz> userQuizzes = userQuizRepository.findByUserIdAndQuizDate(userId, quizDate);
        
        return userQuizzes.stream()
                .map(uq -> UserQuizDto.fromQuestionOnly(uq, uq.getQuiz(), null))
                .toList();
    }

    public UserQuizDto findUserQuizById(Long userQuizId) {
        UserQuiz userQuiz = userQuizRepository.findById(userQuizId)
                .orElseThrow(() -> new UserQuizNotFoundException(String.valueOf(userQuizId)));
        return UserQuizDto.from(userQuiz, userQuiz.getQuiz(), null);
    }

    public Long findQuizIdByUserQuizId(Long userQuizId) {
        Long quizId = userQuizRepository.findQuizIdByUserQuizId(userQuizId);
        if (quizId == null) {
            throw new UserQuizNotFoundException(String.valueOf(userQuizId));
        }
        return quizId;
    }

    public int findQuizHistoryCountByUserId(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate thisWeekMonday = today.with(java.time.DayOfWeek.MONDAY);
        LocalDateTime start = thisWeekMonday.atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        return quizHistoryRepository.countByUserIdAndAnsweredAtBetween(userId, start, end);
    }
}
