package com.freedom.news.domain.service;

import com.freedom.news.domain.entity.NewsReadHistory;
import com.freedom.news.infra.repository.NewsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NewsHistorySaveService {

    private final NewsHistoryRepository newsHistoryRepository;

    public void saveNewsHistory(Long userId, Long newsId) {
        NewsReadHistory history = newsHistoryRepository.findByUserIdAndNewsArticleId(userId, newsId);
        if(history == null) {
            NewsReadHistory newHistory = NewsReadHistory.builder()
                    .userId(userId)
                    .newsArticleId(newsId)
                    .readAt(LocalDateTime.now())
                    .build();
            newsHistoryRepository.save(newHistory);
        }
    }
}
