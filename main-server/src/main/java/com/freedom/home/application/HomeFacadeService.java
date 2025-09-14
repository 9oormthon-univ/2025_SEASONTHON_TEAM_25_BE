package com.freedom.home.application;

import com.freedom.auth.domain.service.AttendanceService;
import com.freedom.auth.domain.service.CharacterNameService;
import com.freedom.home.api.response.AttendanceResponse;
import com.freedom.home.api.response.HomeResponse;
import com.freedom.quiz.domain.service.FindUserQuizService;
import com.freedom.wallet.application.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HomeFacadeService {

    private final WalletService walletService;
    private final FindUserQuizService findUserQuizService;
    private final AttendanceService attendanceService;
    private final CharacterNameService characterNameService;

    @Transactional(readOnly = true)
    public HomeResponse getMainHomeData(Long userId) {
        String characterName = characterNameService.getCharacterName(userId);
        boolean attendance = attendanceService.isAttendanceCompleted(userId);
        BigDecimal balance = walletService.getWalletByUserId(userId).getBalance();
        long correctCount = findUserQuizService.findDailyQuizzes(userId, LocalDate.now()).stream()
                .filter(q -> Boolean.TRUE.equals(q.getIsCorrect()))
                .count();

        return HomeResponse.of(
                characterName,
                balance,
                attendance,
                (int) correctCount
        );
    }

    @Transactional
    public AttendanceResponse markAttendance(Long userId) {
        boolean isSuccess = attendanceService.markAttendance(userId);
        
        return isSuccess
                ? AttendanceResponse.success()
                : AttendanceResponse.alreadyAttended();

    }
}
