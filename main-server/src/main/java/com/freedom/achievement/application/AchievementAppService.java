package com.freedom.achievement.application;

import com.freedom.achievement.api.response.AchievementListResponse;
import com.freedom.achievement.api.response.ClaimAchievementResponse;
import com.freedom.achievement.application.dto.AchievementDto;
import com.freedom.achievement.domain.service.AchievementReadService;
import com.freedom.achievement.domain.service.AchievementCommandService;
import com.freedom.common.logging.Loggable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AchievementAppService {
    
    private final AchievementReadService achievementReadService;
    private final AchievementCommandService achievementCommandService;
    
    @Loggable("사용자 업적 목록 조회")
    public AchievementListResponse getUserAchievements(Long userId) {
        List<AchievementDto> achievements = achievementReadService.getUserAchievements(userId);
        return AchievementListResponse.from(achievements);
    }
    
    @Loggable("업적 확인 처리")
    @Transactional
    public ClaimAchievementResponse claimAchievement(Long userId, Long achievementId) {
        AchievementDto achievementDto = achievementCommandService.claimAchievement(userId, achievementId);
        return ClaimAchievementResponse.from(achievementDto);
    }
}
