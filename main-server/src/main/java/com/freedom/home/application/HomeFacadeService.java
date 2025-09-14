package com.freedom.home.application;

import com.freedom.attendance.domain.service.AttendanceReadService;
import com.freedom.auth.domain.service.CharacterNameService;
import com.freedom.home.api.response.HomeResponse;
import com.freedom.quiz.domain.service.FindUserQuizService;
import com.freedom.wallet.application.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeFacadeService {

    private final WalletService walletService;
    private final FindUserQuizService findUserQuizService;
    private final AttendanceReadService attendanceService;
    private final CharacterNameService characterNameService;
    
    public HomeResponse getMainHomeData(Long userId) {
        CompletableFuture<String> characterNameFuture = CompletableFuture
            .supplyAsync(() -> {
                return characterNameService.getCharacterName(userId);
            });
        
        CompletableFuture<Boolean> attendanceFuture = CompletableFuture
            .supplyAsync(() -> {
                return attendanceService.isAttendanceCompleted(userId);
            });
        
        CompletableFuture<BigDecimal> balanceFuture = CompletableFuture
            .supplyAsync(() -> {
                return walletService.getWalletByUserId(userId).getBalance();
            });
        
        CompletableFuture<Integer> quizCountFuture = CompletableFuture
            .supplyAsync(() -> {
                long correctCount = findUserQuizService.findDailyQuizzes(userId, LocalDate.now()).stream()
                    .filter(q -> Boolean.TRUE.equals(q.getIsCorrect()))
                    .count();
                return (int) correctCount;
            });

        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                characterNameFuture, attendanceFuture, balanceFuture, quizCountFuture
            );
            
            allFutures.get(3, TimeUnit.SECONDS);
            
            return HomeResponse.of(
                characterNameFuture.get(),
                balanceFuture.get(),
                attendanceFuture.get(),
                quizCountFuture.get()
            );
            
        } catch (TimeoutException e) {
            log.warn("홈 데이터 조회 비동기 처리 타임아웃, 동기 처리로 fallback. userId: {}", userId);
            return getMainHomeDataSync(userId);
        } catch (Exception e) {
            log.error("홈 데이터 조회 비동기 처리 중 오류 발생, 동기 처리로 fallback. userId: {}", userId, e);
            return getMainHomeDataSync(userId);
        }
    }
    
    private HomeResponse getMainHomeDataSync(Long userId) {
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
}
