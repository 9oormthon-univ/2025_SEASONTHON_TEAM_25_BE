package com.freedom.performance;

import com.freedom.common.test.TestContainerConfig;
import com.freedom.home.api.response.HomeResponse;
import com.freedom.home.application.HomeFacadeService;
import com.freedom.attendance.domain.service.AttendanceReadService;
import com.freedom.auth.domain.service.CharacterNameService;
import com.freedom.quiz.domain.service.FindUserQuizService;
import com.freedom.wallet.application.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.*;

@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true"
})
@Slf4j
@Sql(scripts = "classpath:test-data.sql")
class HomeFacadePerformanceTest extends TestContainerConfig {

    @Autowired private HomeFacadeService homeFacadeService;
    @Autowired private CharacterNameService characterNameService;
    @Autowired private AttendanceReadService attendanceService;
    @Autowired private WalletService walletService;
    @Autowired private FindUserQuizService findUserQuizService;

    private static final int WARMUP = 3;
    private static final int RUNS   = 100;
    private static final Long USER_ID = 1L; // test-data.sql에 있는 사용자 ID 사용

    @Test
    @DisplayName("HomeFacade 비동기 vs 동기 — 간단 지표(avg/min/max)")
    void compareAsyncVsSync() {
        for (int i = 0; i < WARMUP; i++) {
            safe(() -> homeFacadeService.getMainHomeData(USER_ID));
            safe(this::callSync);
        }

        List<Long> asyncNanos = new ArrayList<>(RUNS);
        List<Long> syncNanos  = new ArrayList<>(RUNS);
        for (int i = 0; i < RUNS; i++) {
            asyncNanos.add(timeNanos(() -> homeFacadeService.getMainHomeData(USER_ID)));
            syncNanos.add(timeNanos(this::callSync));
        }

        Stats a = Stats.of(asyncNanos);
        Stats s = Stats.of(syncNanos);

        double improveAvg = pctImprove(s.avgMs, a.avgMs);

        // ✨ 깔끔한 로그 출력
        log.info("⚡ Async : avg={}ms | min={}ms | max={}ms (n={})",
                fmt2(a.avgMs), fmt2(a.minMs), fmt2(a.maxMs), a.n);
        log.info("🐢 Sync  : avg={}ms | min={}ms | max={}ms (n={})",
                fmt2(s.avgMs), fmt2(s.minMs), fmt2(s.maxMs), s.n);
        log.info("🚀 Result: 평균 {}% 개선 (Sync→Async)", fmt2(improveAvg));
    }

    private void callSync() {
        String name        = characterNameService.getCharacterName(USER_ID);
        boolean attendance = attendanceService.isAttendanceCompleted(USER_ID);
        var balance        = walletService.getWalletByUserId(USER_ID).getBalance();
        int correctCount   = (int) findUserQuizService.findDailyQuizzes(USER_ID, LocalDate.now())
                .stream().filter(q -> Boolean.TRUE.equals(q.getIsCorrect())).count();
        HomeResponse.of(name, balance, attendance, correctCount);
    }

    private static long timeNanos(Runnable r) { long s = System.nanoTime(); r.run(); return System.nanoTime() - s; }
    private static void safe(Runnable r) { try { r.run(); } catch (Exception ignore) {} }
    private static String fmt2(double v) { return String.format(Locale.ROOT, "%.2f", v); }
    private static double pctImprove(double fromMs, double toMs) { return fromMs > 0 ? ((fromMs - toMs) / fromMs) * 100.0 : 0.0; }

    // ── Stats: min/max/avg만 ──
    private record Stats(double avgMs, double minMs, double maxMs, int n) {
        static Stats of(List<Long> ns) {
            if (ns.isEmpty()) return new Stats(0,0,0,0);
            List<Long> s = ns.stream().sorted().toList();
            int n = s.size();
            double avg = s.stream().mapToLong(Long::longValue).average().orElse(0) / 1_000_000.0;
            double min = s.get(0) / 1_000_000.0;
            double max = s.get(n-1) / 1_000_000.0;
            return new Stats(avg, min, max, n);
        }
    }

}
