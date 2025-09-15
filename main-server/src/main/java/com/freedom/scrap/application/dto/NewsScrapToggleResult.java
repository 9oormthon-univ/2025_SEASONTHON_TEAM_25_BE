package com.freedom.scrap.application.dto;

import com.freedom.achievement.application.dto.AchievementDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NewsScrapToggleResult {
    
    private final Long newsArticleId;
    private final boolean isScraped;
    private final String message;
    private final String achievementType;
    private final boolean achievementCreated;
    
    public static NewsScrapToggleResult of(Long newsArticleId, boolean isScraped, AchievementDto achievementDto) {
        String message = isScraped ? "스크랩이 등록되었습니다." : "스크랩이 해제되었습니다.";
        return NewsScrapToggleResult.builder()
                .newsArticleId(newsArticleId)
                .isScraped(isScraped)
                .message(message)
                .achievementType(achievementDto != null ? achievementDto.getType() : null)
                .achievementCreated(achievementDto != null)
                .build();
    }
}
