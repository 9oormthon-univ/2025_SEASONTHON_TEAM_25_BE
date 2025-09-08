package com.freedom.news.application.schedule;

import com.freedom.news.application.facade.NewsFacade;
import com.freedom.common.notification.DiscordWebhookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

@Component
@RequiredArgsConstructor
public class NewsScheduler {
    
    private final NewsFacade newsFacade;
    private final DiscordWebhookClient discordWebhookClient;
    
    @Scheduled(cron = "${news.scheduler.cron}")
    public void scheduleNewsCollection() {
        try {
            newsFacade.newsCollection();
        } catch (Exception e) {
            String stackTrace = getStackTraceAsString(e);
            String errorMessage = e.getMessage();
            
            // 중복 키 오류의 경우 더 친화적인 메시지로 변경
            if (errorMessage != null && errorMessage.contains("Duplicate entry")) {
                errorMessage = "뉴스 중복 저장 시도 - 이미 존재하는 뉴스입니다. 시스템이 자동으로 처리합니다.";
            }

            discordWebhookClient.sendErrorMessage(
                    "🚨 뉴스 수집 스케줄러 오류",
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
