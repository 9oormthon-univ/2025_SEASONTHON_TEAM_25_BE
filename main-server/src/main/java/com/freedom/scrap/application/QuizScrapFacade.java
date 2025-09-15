package com.freedom.scrap.application;

import com.freedom.achievement.application.dto.AchievementDto;
import com.freedom.achievement.domain.entity.Achievement;
import com.freedom.achievement.domain.service.AchievementCommandService;
import com.freedom.auth.domain.User;
import com.freedom.auth.domain.service.FindUserService;
import com.freedom.common.dto.PageResponse;
import com.freedom.common.logging.Loggable;
import com.freedom.quiz.application.dto.UserQuizDto;
import com.freedom.quiz.domain.service.FindUserQuizService;
import com.freedom.scrap.application.dto.QuizScrapDto;
import com.freedom.scrap.application.dto.QuizScrapToggleResult;
import com.freedom.scrap.domain.entity.QuizScrap;
import com.freedom.scrap.domain.entity.ScrapHistory;
import com.freedom.scrap.domain.service.FindQuizScrapService;
import com.freedom.scrap.domain.service.FindScrapHistoryService;
import com.freedom.scrap.domain.service.QuizScrapToggleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizScrapFacade {
    
    private final QuizScrapToggleService quizScrapToggleService;
    private final FindQuizScrapService findQuizScrapService;
    private final FindUserService findUserService;
    private final FindUserQuizService findUserQuizService;
    private final FindScrapHistoryService findScrapHistoryService;
    private final AchievementCommandService achievementCommandService;

    @Loggable("퀴즈 스크랩 토글")
    @Transactional
    public QuizScrapToggleResult toggleQuizScrap(Long userId, Long userQuizId, Boolean isCorrectAtScrap) {
        User user = findUserService.findById(userId);
        UserQuizDto userQuiz = findUserQuizService.findUserQuizById(userQuizId);
        boolean isScraped = quizScrapToggleService.toggleQuizScrap(user, userQuiz.getUserQuizId(), isCorrectAtScrap);
        AchievementDto achievementDto = null;
        if(isScraped){
            int count = findScrapHistoryService.getTotalScrapCountByType(userId, ScrapHistory.ScrapType.QUIZ);
            if(count == 50){
                achievementDto = achievementCommandService.grantAchievement(userId, Achievement.AchievementType.QUIZ_CURATOR);
            }
        }
        return QuizScrapToggleResult.of(userQuizId, isScraped, isCorrectAtScrap, achievementDto);
    }
    
    @Loggable("사용자 퀴즈 스크랩 목록 조회")
    public PageResponse<QuizScrapDto> getQuizScrapList(Long userId, Pageable pageable) {
        Page<QuizScrap> quizScrapPage = findQuizScrapService.findQuizScrapsByUserId(userId, pageable);
        Page<QuizScrapDto> quizScrapDtoPage = quizScrapPage.map(QuizScrapDto::from);
        return PageResponse.of(quizScrapDtoPage);
    }
}
