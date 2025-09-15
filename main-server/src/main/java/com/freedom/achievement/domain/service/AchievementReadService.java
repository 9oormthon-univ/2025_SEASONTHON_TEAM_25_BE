package com.freedom.achievement.domain.service;

import com.freedom.achievement.application.dto.AchievementDto;
import com.freedom.achievement.domain.entity.UserAchievement;
import com.freedom.achievement.infra.UserAchievementRepository;
import com.freedom.common.logging.Loggable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementReadService {
    
    private final UserAchievementRepository userAchievementRepository;
    
    @Loggable("사용자 업적 목록 조회")
    public List<AchievementDto> getUserAchievements(Long userId) {
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);
        return userAchievements.stream()
                .map(AchievementDto::from)
                .toList();
    }
}
