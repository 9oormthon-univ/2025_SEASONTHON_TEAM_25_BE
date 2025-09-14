package com.freedom.quest.domain.entity;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quests")
public class Quest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "progress_mode", nullable = false, length = 20)
    private ProgressMode progressMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private TargetType targetType;

    @Column(nullable = false)
    private int requirementCount;

    @Column(nullable = false)
    private BigDecimal rewardAmount;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
