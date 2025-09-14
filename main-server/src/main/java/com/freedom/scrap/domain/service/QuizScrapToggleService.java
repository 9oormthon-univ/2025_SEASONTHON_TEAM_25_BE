package com.freedom.scrap.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.common.logging.Loggable;
import com.freedom.quiz.domain.entity.UserQuiz;
import com.freedom.scrap.domain.entity.QuizScrap;
import com.freedom.scrap.domain.entity.ScrapHistory;
import com.freedom.scrap.infra.QuizScrapRepository;
import com.freedom.scrap.infra.ScrapHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuizScrapToggleService {
    
    private final QuizScrapRepository quizScrapRepository;
    private final ScrapHistoryRepository scrapHistoryRepository;

    @Loggable("퀴즈 스크랩 토글")
    public boolean toggleQuizScrap(User user, Long userQuizId, Boolean isCorrectAtScrap) {
        return quizScrapRepository.findByUserIdAndUserQuizId(user.getId(), userQuizId)
                .map(existingScrap -> {
                    quizScrapRepository.delete(existingScrap);
                    return false; // 해제됨
                })
                .orElseGet(() -> {
                    QuizScrap quizScrap = QuizScrap.create(user, UserQuiz.createUserQuiz(userQuizId), isCorrectAtScrap);
                    quizScrapRepository.save(quizScrap);
                    ScrapHistory history = scrapHistoryRepository.findByUserIdAndScrapIdAndType(user.getId(), userQuizId, ScrapHistory.ScrapType.QUIZ);
                    if (history == null) {
                        ScrapHistory scrapHistory = ScrapHistory.builder().userId(user.getId()).scrapId(userQuizId).type(ScrapHistory.ScrapType.QUIZ).scrapAt(LocalDateTime.now()).build();
                        scrapHistoryRepository.save(scrapHistory);
                    }
                    return true; // 등록됨
                });
    }
}
