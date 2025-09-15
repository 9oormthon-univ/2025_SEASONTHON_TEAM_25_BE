package com.freedom.achievement.domain.service;

import com.freedom.achievement.application.dto.AchievementDto;
import com.freedom.achievement.domain.entity.Achievement;
import com.freedom.achievement.domain.entity.UserAchievement;
import com.freedom.achievement.infra.AchievementRepository;
import com.freedom.achievement.infra.UserAchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementCommandService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    public AchievementDto grantAchievement(Long userId, Achievement.AchievementType achievementType) {
        if(userAchievementRepository.existsByUserIdAndAchievement_Type(userId, achievementType)){
            log.warn("이미 해당 업적을 보유하고 있습니다. userId: {}, achievementType: {}", userId, achievementType);
            return null;
        }
        Achievement achievement = achievementRepository.findByType(achievementType).orElseThrow(() -> new IllegalArgumentException("해당 업적이 존재하지 않습니다. achievementType: " + achievementType));
        UserAchievement userAchievement = UserAchievement.create(userId, achievement);
        Achievement saveAchievement = userAchievementRepository.save(userAchievement).getAchievement();
        return AchievementDto.toDto(saveAchievement);
    }
    
    public AchievementDto claimAchievement(Long userId, Long achievementId) {
        UserAchievement userAchievement = userAchievementRepository.findByUserIdAndAchievement_Id(userId, achievementId)
                .orElseThrow(() -> new IllegalArgumentException("해당 업적을 보유하고 있지 않습니다."));
        
        if (userAchievement.isClaimed()) {
            throw new IllegalArgumentException("이미 확인한 업적입니다.");
        }
        
        userAchievement.claim();
        userAchievementRepository.save(userAchievement);
        
        return AchievementDto.from(userAchievement);
    }
}
