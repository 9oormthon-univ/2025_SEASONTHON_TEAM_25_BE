package com.freedom.achievement.api.response;

import com.freedom.achievement.application.dto.AchievementDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AchievementListResponse {
    
    private final List<AchievementDto> achievements;
    
    public static AchievementListResponse from(List<AchievementDto> achievements) {
        return new AchievementListResponse(achievements);
    }
}
