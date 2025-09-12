package com.freedom.scrap.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.news.domain.entity.NewsArticle;
import com.freedom.scrap.domain.entity.NewsScrap;
import com.freedom.scrap.infra.NewsScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsScrapToggleService {
    
    private final NewsScrapRepository newsScrapRepository;
    
    public boolean toggleNewsScrap(User user, Long newsId) {
        return newsScrapRepository.findByUserIdAndNewsArticleId(user.getId(), newsId)
                .map(existingScrap -> {
                    newsScrapRepository.delete(existingScrap);
                    return false;
                })
                .orElseGet(() -> {
                    NewsScrap newsScrap = NewsScrap.create(user, NewsArticle.createNewsArticle(newsId));
                    newsScrapRepository.save(newsScrap);
                    return true;
                });
    }
}
