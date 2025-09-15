package com.freedom.scrap.application.dto;

import com.freedom.achievement.application.dto.AchievementDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizScrapToggleResult {
    
    private final Long userQuizId;
    private final boolean isScraped;
    private final Boolean isCorrectAtScrap;
    private final String message;
    private final String achievementType;
    private final boolean achievementCreated;
    
    public static QuizScrapToggleResult of(Long userQuizId, boolean isScraped, Boolean isCorrectAtScrap, AchievementDto achievementDto) {
        String message = isScraped ? "퀴즈 스크랩이 등록되었습니다." : "퀴즈 스크랩이 해제되었습니다.";
        return QuizScrapToggleResult.builder()
                .userQuizId(userQuizId)
                .isScraped(isScraped)
                .isCorrectAtScrap(isScraped ? isCorrectAtScrap : null)
                .message(message)
                .achievementType(achievementDto != null ? achievementDto.getType() : null)
                .achievementCreated(achievementDto != null)
                .build();
    }
}
