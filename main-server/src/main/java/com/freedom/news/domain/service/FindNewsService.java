package com.freedom.news.domain.service;

import com.freedom.common.exception.custom.NewsNotFoundException;
import com.freedom.news.application.dto.NewsDetailDto;
import com.freedom.news.application.dto.NewsDto;
import com.freedom.news.domain.entity.NewsArticle;
import com.freedom.news.infra.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FindNewsService {
    
    private final NewsArticleRepository newsArticleRepository;

    @Transactional(readOnly = true)
    public Page<NewsDto> findRecentNews(Pageable pageable) {
        LocalDate today = LocalDate.now();
        
        // 이번 주 월요일 ~ 일요일 범위 계산
        LocalDate thisWeekMonday = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate thisWeekSunday = thisWeekMonday.plusDays(6);
        LocalDateTime thisWeekStart = thisWeekMonday.atStartOfDay();
        LocalDateTime thisWeekEnd = thisWeekSunday.plusDays(1).atStartOfDay();

        Page<NewsArticle> newsArticles = newsArticleRepository.findRecentNewsByApproveDateBetween(
            thisWeekStart, thisWeekEnd, pageable
        );
        
        // 이번 주에 뉴스가 없으면 전주 뉴스로 fallback
        if (newsArticles.isEmpty()) {
            LocalDate lastWeekMonday = thisWeekMonday.minusWeeks(1);
            LocalDate lastWeekSunday = lastWeekMonday.plusDays(6);
            LocalDateTime lastWeekStart = lastWeekMonday.atStartOfDay();
            LocalDateTime lastWeekEnd = lastWeekSunday.plusDays(1).atStartOfDay();
            
            newsArticles = newsArticleRepository.findRecentNewsByApproveDateBetween(
                lastWeekStart, lastWeekEnd, pageable
            );
        }
        
        return newsArticles.map(NewsDto::from);
    }

    @Transactional(readOnly = true)
    public NewsDetailDto findNewsById(Long newsId) {
        NewsArticle newsArticle = newsArticleRepository.findById(newsId).orElseThrow(() -> new NewsNotFoundException("존재하지 않는 뉴스 입니다." + newsId));
        return NewsDetailDto.from(newsArticle, newsArticle.getContentBlocks());
    }
}
