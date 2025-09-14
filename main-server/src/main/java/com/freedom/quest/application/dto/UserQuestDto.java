package com.freedom.quest.application.dto;

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
}
