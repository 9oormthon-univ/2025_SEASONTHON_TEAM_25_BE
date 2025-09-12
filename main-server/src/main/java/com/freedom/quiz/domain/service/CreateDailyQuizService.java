package com.freedom.quiz.domain.service;

import com.freedom.quiz.application.dto.UserQuizDto;
import com.freedom.quiz.domain.entity.Quiz;
import com.freedom.quiz.domain.entity.UserQuiz;
import com.freedom.common.exception.custom.InsufficientQuizException;
import com.freedom.quiz.infra.QuizRepository;
import com.freedom.quiz.infra.UserQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateDailyQuizService {

    private final QuizRepository quizRepository;
    private final UserQuizRepository userQuizRepository;
    
    private static final int DAILY_QUIZ_COUNT = 5;
    private static final int NEWS_QUIZ_TARGET_COUNT = 4;
    private static final Long EMPTY_LIST_DUMMY_ID = -1L;

    public List<UserQuizDto> createDailyQuizzes(Long userId, LocalDate quizDate) {
        LocalDate monday = quizDate.with(DayOfWeek.MONDAY);
        LocalDate sunday = quizDate.with(DayOfWeek.SUNDAY);
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = sunday.atTime(23, 59, 59);
        
        List<Long> excludeQuizIds = userQuizRepository.findQuizIdsByUserIdAndDateRange(userId, monday, sunday);
        List<Long> safeExcludeIds = excludeQuizIds.isEmpty() ? List.of(EMPTY_LIST_DUMMY_ID) : excludeQuizIds;
        
        List<Quiz> newsQuizzes = quizRepository.findNewsQuizzesInWeek(weekStart, weekEnd, safeExcludeIds);
        
        // 뉴스 퀴즈가 4개를 초과하면 4개로 제한
        if (newsQuizzes.size() > NEWS_QUIZ_TARGET_COUNT) {
            Collections.shuffle(newsQuizzes);
            newsQuizzes = new ArrayList<>(newsQuizzes.subList(0, NEWS_QUIZ_TARGET_COUNT));
        }
        
        // 남은 개수만큼 일반 퀴즈 가져오기
        int remainingCount = DAILY_QUIZ_COUNT - newsQuizzes.size();
        List<Quiz> generalQuizzes = quizRepository.findGeneralQuizzes(safeExcludeIds, remainingCount);

        List<Quiz> selectedQuizzes = new ArrayList<>(newsQuizzes);
        
        // 총 5개를 초과하지 않도록 일반 퀴즈 추가
        int canAddCount = DAILY_QUIZ_COUNT - selectedQuizzes.size();
        if (generalQuizzes.size() > canAddCount) {
            selectedQuizzes.addAll(generalQuizzes.subList(0, canAddCount));
        } else {
            selectedQuizzes.addAll(generalQuizzes);
        }
        
        if (selectedQuizzes.isEmpty()) {
            throw new InsufficientQuizException("출제 가능한 퀴즈가 없습니다.");
        }

        List<UserQuiz> userQuizzes = selectedQuizzes.stream()
                .map(quiz -> UserQuiz.builder()
                        .userId(userId)
                        .quiz(quiz)
                        .quizDate(quizDate)
                        .assignedDate(quizDate)
                        .build())
                .toList();

        List<UserQuiz> savedUserQuizzes = userQuizRepository.saveAll(userQuizzes);
        return savedUserQuizzes.stream()
                .map(uq -> UserQuizDto.fromQuestionOnly(uq, uq.getQuiz(), null))
                .toList();
    }
}
