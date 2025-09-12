package com.freedom.scrap.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.common.logging.Loggable;
import com.freedom.quiz.domain.entity.UserQuiz;
import com.freedom.scrap.domain.entity.QuizScrap;
import com.freedom.scrap.infra.QuizScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizScrapToggleService {
    
    private final QuizScrapRepository quizScrapRepository;
    
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
                    return true; // 등록됨
                });
    }
}
