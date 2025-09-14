package com.freedom.quest.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class QuestDto {
    private Long questId;
    private String title;
    private String description;
    private String periodType;
    private String progressMode;
    private String targetType;
    private int requirementCount;
    private BigDecimal rewardAmount;
    private boolean active;
}
