package com.freedom.home.api.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class HomeResponse {

    private String characterName;
    private BigDecimal balance;
    private boolean attendance;
    private int quizCount;
    
    public static HomeResponse of(String characterName, BigDecimal balance,boolean attendance, int quizCount) {
        return HomeResponse.builder()
                .characterName(characterName)
                .balance(balance)
                .attendance(attendance)
                .quizCount(quizCount)
                .build();
    }
}
