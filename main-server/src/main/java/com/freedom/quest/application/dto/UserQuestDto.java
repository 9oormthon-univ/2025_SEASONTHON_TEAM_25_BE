package com.freedom.quest.application.dto;

import com.freedom.quest.domain.entity.UserQuest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserQuestDto {
    private Long userQuestId;
    private QuestDto quest;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private int progressCount;
    private int currentStreak;
    private boolean completed;
    private boolean claimed;

    public static UserQuestDto toDto(UserQuest userQuest) {
        return UserQuestDto.builder()
                    .userQuestId(userQuest.getId())
                    .quest(QuestDto.builder()
                            .questId(userQuest.getQuest().getId())
                            .title(userQuest.getQuest().getTitle())
                            .description(userQuest.getQuest().getDescription())
                            .periodType(userQuest.getQuest().getPeriodType().name())
                            .progressMode(userQuest.getQuest().getProgressMode().name())
                            .targetType(userQuest.getQuest().getTargetType().name())
                            .requirementCount(userQuest.getQuest().getRequirementCount())
                            .rewardAmount(userQuest.getQuest().getRewardAmount())
                            .active(userQuest.getQuest().isActive())
                            .build())
                    .periodStartDate(userQuest.getPeriodStartDate())
                    .periodEndDate(userQuest.getPeriodEndDate())
                    .progressCount(userQuest.getProgressCount())
                    .currentStreak(userQuest.getCurrentStreak())
                    .completed(userQuest.isCompleted())
                    .claimed(userQuest.isClaimed())
                    .build();
    }
}
