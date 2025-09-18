package com.freedom.quest.domain.entity;

import com.freedom.auth.domain.User;
import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(
        name = "user_quests",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_quest_period",
                        columnNames = {"user_id", "quest_id", "period_start_date"})
        }
)
public class UserQuest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    @Column(nullable = false)
    private int progressCount;

    @Column(nullable = false)
    private int currentStreak;

    @Column(name = "is_claimed", nullable = false)
    private boolean isClaimed;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Builder
    public UserQuest(User user, Quest quest, LocalDate periodStartDate, LocalDate periodEndDate, int progressCount, int currentStreak) {
        this.user = user;
        this.quest = quest;
        this.periodStartDate = periodStartDate;
        this.periodEndDate = periodEndDate;
        this.progressCount = progressCount;
        this.currentStreak = currentStreak;
        this.isClaimed = false;
        this.completed = false;
    }

    public void updateCurrentStreak(int streak) {
        this.currentStreak = Math.max(0, streak);
    }

    public void completeQuest() {
        this.completed = true;
    }

    public void claimReward() {
        this.isClaimed = true;
    }

    public void updateProgressCount(int checkCount,int count) {
        this.progressCount = Math.min(checkCount, count);
    }
}
