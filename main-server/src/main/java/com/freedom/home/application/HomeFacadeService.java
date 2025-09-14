package com.freedom.home.application;

import com.freedom.auth.domain.User;
import com.freedom.auth.domain.service.FindUserService;
import com.freedom.home.api.dto.HomeResponse;
import com.freedom.quiz.application.dto.UserQuizDto;
import com.freedom.quiz.domain.service.FindUserQuizService;
import com.freedom.wallet.application.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeAppService {

    private final FindUserService findUserService;
    private final WalletService walletService;
    private final FindUserQuizService findUserQuizService;

    public HomeResponse getHome(Long userId) {
        boolean attendance = findUserService.findById(userId).getAttendance();
        BigDecimal balance = walletService.getWalletByUserId(userId).getBalance();
        
        LocalDate today = LocalDate.now();
        List<UserQuizDto> todays = findUserQuizService.findDailyQuizzes(userId, today);

        return null;
    }
}



