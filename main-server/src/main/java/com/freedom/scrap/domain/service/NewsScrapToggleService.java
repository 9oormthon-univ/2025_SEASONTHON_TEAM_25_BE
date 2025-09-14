package com.freedom.scrap.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.news.domain.entity.NewsArticle;
import com.freedom.scrap.domain.entity.NewsScrap;
import com.freedom.scrap.domain.entity.ScrapHistory;
import com.freedom.scrap.infra.NewsScrapRepository;
import com.freedom.scrap.infra.ScrapHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NewsScrapToggleService {
    
    private final NewsScrapRepository newsScrapRepository;
    private final ScrapHistoryRepository scrapHistoryRepository;

    public boolean toggleNewsScrap(User user, Long newsId) {
        return newsScrapRepository.findByUserIdAndNewsArticleId(user.getId(), newsId)
                .map(existingScrap -> {
                    newsScrapRepository.delete(existingScrap);
                    return false;
                })
                .orElseGet(() -> {
                    NewsScrap newsScrap = NewsScrap.create(user, NewsArticle.createNewsArticle(newsId));
                    newsScrapRepository.save(newsScrap);
                    ScrapHistory history = scrapHistoryRepository.findByUserIdAndScrapIdAndType(user.getId(), newsId, ScrapHistory.ScrapType.NEWS);
                    if (history == null) {
                        ScrapHistory scrapHistory = ScrapHistory.builder().userId(user.getId()).scrapId(newsId).type(ScrapHistory.ScrapType.NEWS).scrapAt(LocalDateTime.now()).build();
                        scrapHistoryRepository.save(scrapHistory);
                    }
                    return true;
                });
    }
}
