package com.freedom.achievement.domain.entity;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
@Table(name = "user_achievement")
public class UserAchievement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "is_claimed", nullable = false)
    private boolean claimed;

    public static UserAchievement create(Long userId, Achievement achievement) {
        return UserAchievement.builder()
                .userId(userId)
                .achievement(achievement)
                .claimed(false)
                .build();
    }
    
    public void claim() {
        this.claimed = true;
    }
}
