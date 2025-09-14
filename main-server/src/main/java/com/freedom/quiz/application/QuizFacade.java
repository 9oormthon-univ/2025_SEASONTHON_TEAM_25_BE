package com.freedom.quiz.application;

import com.freedom.quiz.api.response.QuizAnswerResultResponse;
import com.freedom.quiz.application.dto.DailyQuizDto;
import com.freedom.quiz.application.dto.UserQuizDto;
import com.freedom.quiz.domain.entity.QuizType;
import com.freedom.quiz.domain.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizFacade {

    private final FindUserQuizService findUserQuizService;
    private final CreateDailyQuizService createDailyQuizService;
    private final UpdateUserQuizService updateUserQuizService;
    private final QuizHistorySaveService quizHistorySaveService;

    @Transactional
    public DailyQuizDto getDailyQuizzes(Long userId) {
        LocalDate today = LocalDate.now();
        
        List<UserQuizDto> dailyUserQuizDtos = findUserQuizService.findDailyQuizzes(userId, today);
        
        if (dailyUserQuizDtos.isEmpty()) {
            dailyUserQuizDtos = createDailyQuizService.createDailyQuizzes(userId, today);
        }
        
        long correctCount = dailyUserQuizDtos.stream()
                .mapToLong(uq -> Boolean.TRUE.equals(uq.getIsCorrect()) ? 1 : 0)
                .sum();
        boolean isCompleted = correctCount == dailyUserQuizDtos.size();
        
        List<UserQuizDto> activeQuizList = dailyUserQuizDtos.stream()
                .filter(uq -> uq.getIsCorrect() == null || !uq.getIsCorrect())
                .toList();

        return DailyQuizDto.from(isCompleted, activeQuizList);
    }

    @Transactional
    public QuizAnswerResultResponse submitQuizAnswer(Long userQuizId, String userAnswer) {
        UserQuizDto userQuizDto = findUserQuizService.findUserQuizById(userQuizId);
        
        boolean isCorrect = userQuizDto.getType() == QuizType.OX
                ? userQuizDto.getOxAnswer() != null &&
                  userQuizDto.getOxAnswer().toString().equalsIgnoreCase(userAnswer)
                : userQuizDto.getMcqCorrectIndex() != null &&
                  userQuizDto.getMcqCorrectIndex().toString().equals(userAnswer);

        quizHistorySave(userQuizDto, isCorrect);

        updateUserQuizService.updateAnswer(userQuizId, userAnswer, isCorrect);
        return QuizAnswerResultResponse.from(isCorrect, userQuizDto);
    }

    private void quizHistorySave(UserQuizDto userQuizDto, boolean isCorrect) {
        if(userQuizDto.getUserAnswer() == null && isCorrect) { // 최초 시도 정답 제출 시에만 히스토리 저장
            quizHistorySaveService.quizHistorySave(userQuizDto.getUserId(), userQuizDto.getQuizId());
        }
    }
}
