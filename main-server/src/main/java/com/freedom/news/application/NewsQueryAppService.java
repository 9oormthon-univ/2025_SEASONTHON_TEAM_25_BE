package com.freedom.news.application;

import com.freedom.news.api.response.NewsDetailResponse;
import com.freedom.news.api.response.NewsResponse;
import com.freedom.news.application.dto.NewsDetailDto;
import com.freedom.news.application.dto.NewsDto;
import com.freedom.news.domain.service.FindNewsService;
import com.freedom.news.domain.service.NewsHistorySaveService;
import com.freedom.scrap.application.dto.NewsScrapDto;
import com.freedom.scrap.domain.service.FindNewsScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsQueryAppService {
    
    private final FindNewsService findNewsService;
    private final FindNewsScrapService findNewsScrapService;
    private final NewsHistorySaveService newsHistorySaveService;

    public Page<NewsResponse> getRecentNewsList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsDto> newsDtos = findNewsService.findRecentNews(pageable);
        
        return newsDtos.map(NewsResponse::from);
    }

    public NewsDetailResponse getNewsDetail(Long newsId, Long userId) {
        NewsDetailDto newsDetailDto = findNewsService.findNewsById(newsId);
        NewsScrapDto newsScrapDto = findNewsScrapService.getNewsScrapById(newsId, userId);
        newsHistorySaveService.saveNewsHistory(userId, newsId);
        return NewsDetailResponse.from(newsDetailDto, newsScrapDto);
    }
}
