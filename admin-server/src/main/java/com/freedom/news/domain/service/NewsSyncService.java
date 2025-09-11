package com.freedom.news.domain.service;

import com.freedom.news.application.schedule.NewsScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 뉴스 동기화 도메인 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSyncService {

    private final NewsScheduler newsScheduler;

    public void syncLatestNews() {
        try {
            newsScheduler.scheduleNewsCollection();
        } catch (Exception e) {
            log.error("관리자에 의한 수동 뉴스 동기화 실패", e);
            throw new RuntimeException("뉴스 동기화에 실패했습니다: " + e.getMessage());
        }
    }
}
