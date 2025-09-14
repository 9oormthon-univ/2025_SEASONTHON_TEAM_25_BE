package com.freedom.quest.api.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class QuestSummaryResponse {
    private Long userQuestId;
    private String title;
    private String description;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int requirementCount;
    private int progressCount;
    private int currentStreak;
    private boolean completed;
    private boolean claimed;
    private BigDecimal rewardAmount;
}
