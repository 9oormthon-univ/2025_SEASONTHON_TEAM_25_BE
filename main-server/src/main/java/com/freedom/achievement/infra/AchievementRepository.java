package com.freedom.achievement.infra;

import com.freedom.achievement.domain.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findByType(Achievement.AchievementType achievementType);
}
