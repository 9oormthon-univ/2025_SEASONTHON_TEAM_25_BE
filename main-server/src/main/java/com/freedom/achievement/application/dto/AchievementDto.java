package com.freedom.achievement.application.dto;

import com.freedom.achievement.domain.entity.Achievement;
import com.freedom.achievement.domain.entity.UserAchievement;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AchievementDto {
    private Long achievementId;
    private String type;
    private String title;
    private String description;
    private String iconUrl;
    private int requirementCount;
    private boolean claimed;

    public static AchievementDto toDto(Achievement achievement) {
        return AchievementDto.builder()
                .achievementId(achievement.getId())
                .type(achievement.getType().name())
                .title(achievement.getTitle())
                .description(achievement.getDescription())
                .iconUrl(achievement.getIconUrl())
                .requirementCount(achievement.getRequirementCount())
                .claimed(false)
                .build();
    }
    
    public static AchievementDto from(UserAchievement userAchievement) {
        Achievement achievement = userAchievement.getAchievement();
        return AchievementDto.builder()
                .achievementId(achievement.getId())
                .type(achievement.getType().name())
                .title(achievement.getTitle())
                .description(achievement.getDescription())
                .iconUrl(achievement.getIconUrl())
                .requirementCount(achievement.getRequirementCount())
                .claimed(userAchievement.isClaimed())
                .build();
    }
}
