package com.freedom.home.api.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.math.BigDecimal;

@Getter
public class HomeResponse {

    private String nickname;

    private BigDecimal balance;

    private LocalDate today;
    
    private int quizCount;
    
    private HomeResponse(String nickname, BigDecimal balance, LocalDate today, int quizCount) {
        this.nickname = nickname;
        this.balance = balance;
        this.today = today;
        this.quizCount = quizCount;
    }
    
    public static HomeResponse of(String nickname, BigDecimal balance, LocalDate today, int quizCount) {
        return new HomeResponse(nickname, balance, today, quizCount);
    }
}
