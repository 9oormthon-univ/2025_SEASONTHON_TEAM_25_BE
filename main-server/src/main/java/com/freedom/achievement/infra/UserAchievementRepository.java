package com.freedom.achievement.infra;

import com.freedom.achievement.domain.entity.Achievement;
import com.freedom.achievement.domain.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    boolean existsByUserIdAndAchievement_Type(Long userId, Achievement.AchievementType achievementType);
    
    List<UserAchievement> findByUserId(Long userId);
    
    Optional<UserAchievement> findByUserIdAndAchievement_Id(Long userId, Long achievementId);
}
