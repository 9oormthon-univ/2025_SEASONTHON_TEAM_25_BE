package com.freedom.attendance.application;

import com.freedom.attendance.domain.service.AttendanceCommandService;
import com.freedom.common.notification.DiscordWebhookClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceCommandService attendanceCommandService;
    private final DiscordWebhookClient discordWebhookClient;

    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyAttendance() {
        try {
            attendanceCommandService.resetAllAttendanceStatus();
        } catch (Exception e) {
            log.error("일일 출석 초기화 중 오류 발생", e);
            String stackTrace = getStackTraceAsString(e);

            String errorMessage = e.getMessage();

            discordWebhookClient.sendErrorMessage(
                    "🚨 출석 초기화 스케줄러 오류",
                    "**오류 메시지:** " + errorMessage +
                            "\n\n**스택 트레이스:**\n```" +
                            (stackTrace.length() > 1500 ? stackTrace.substring(0, 1500) + "..." : stackTrace) +
                            "```"
            );
        }
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
