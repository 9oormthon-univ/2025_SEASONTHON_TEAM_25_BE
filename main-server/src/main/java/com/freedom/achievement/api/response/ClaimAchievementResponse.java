package com.freedom.achievement.api.response;

import com.freedom.achievement.application.dto.AchievementDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClaimAchievementResponse {
    
    private final Long achievementId;
    private final String type;
    private final String title;
    private final String description;
    private final String iconUrl;
    private final boolean claimed;
    private final String message;
    
    public static ClaimAchievementResponse from(AchievementDto achievementDto) {
        return new ClaimAchievementResponse(
                achievementDto.getAchievementId(),
                achievementDto.getType(),
                achievementDto.getTitle(),
                achievementDto.getDescription(),
                achievementDto.getIconUrl(),
                true,
                "업적을 확인했습니다."
        );
    }
}
