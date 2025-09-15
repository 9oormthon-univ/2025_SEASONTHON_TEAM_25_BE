package com.freedom.achievement.domain.entity;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Table(name = "achievements")
public class Achievement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AchievementType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "icon_url", nullable = false)
    private String iconUrl;

    @Column(name = "requirement_count", nullable = false)
    private int requirementCount;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public enum AchievementType {
        BEGINNERS_LUCK,         // 초심자의 행운
        MORNING_SUNSHINE,       // 아침 햇살
        MONEY_MAKER,            // 머니 메이커
        NEWS_ADDICT,            // 속보 중독
        NEWS_COLLECTOR,         // 소식좌
        QUIZ_CURATOR,           // 퀴즈 큐레이터
        MATURITY_MASTER,        // 만기의 달인
        ROYAL_FINANCER,         // 로얄 파이낸서
        ANTI_FRAGILE,           // 안티 프레자일
        ESCAPE_CRISIS,          // 위기탈출
        GOLDEN_BRAIN            // 골든 브레인
    }
}
