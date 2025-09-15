package com.freedom.news.application;

import com.freedom.achievement.application.dto.AchievementDto;
import com.freedom.achievement.domain.entity.Achievement;
import com.freedom.achievement.domain.service.AchievementCommandService;
import com.freedom.news.api.response.NewsDetailResponse;
import com.freedom.news.api.response.NewsResponse;
import com.freedom.news.application.dto.NewsDetailDto;
import com.freedom.news.application.dto.NewsDto;
import com.freedom.news.domain.service.FindNewsService;
import com.freedom.news.domain.service.NewsHistoryReadService;
import com.freedom.news.domain.service.NewsHistorySaveService;
import com.freedom.scrap.application.dto.NewsScrapDto;
import com.freedom.scrap.domain.service.FindNewsScrapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsQueryAppService {
    
    private final FindNewsService findNewsService;
    private final FindNewsScrapService findNewsScrapService;
    private final NewsHistorySaveService newsHistorySaveService;
    private final NewsHistoryReadService newsHistoryReadService;
    private final AchievementCommandService achievementCommandService;

    public Page<NewsResponse> getRecentNewsList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDto> newsDtos = findNewsService.findRecentNews(pageable);
        
        return newsDtos.map(NewsResponse::from);
    }

    public NewsDetailResponse getNewsDetail(Long newsId, Long userId) {
        CompletableFuture<NewsDetailDto> newsDetailFuture = CompletableFuture.supplyAsync(() ->
                findNewsService.findNewsById(newsId)
        );
        CompletableFuture<NewsScrapDto> newsScrapFuture = CompletableFuture.supplyAsync(() ->
                findNewsScrapService.getNewsScrapById(newsId, userId)
        );
        newsHistorySaveService.saveNewsHistory(userId, newsId);
        int count = newsHistoryReadService.countNewsRead(userId);
        AchievementDto achievementDto = null;
        if(count == 50){
            achievementDto = achievementCommandService.grantAchievement(userId, Achievement.AchievementType.NEWS_ADDICT);
        }
        NewsDetailDto newsDetailDto = newsDetailFuture.join();
        NewsScrapDto newsScrapDto = newsScrapFuture.join();
        return NewsDetailResponse.of(newsDetailDto, newsScrapDto, achievementDto);
    }
}
