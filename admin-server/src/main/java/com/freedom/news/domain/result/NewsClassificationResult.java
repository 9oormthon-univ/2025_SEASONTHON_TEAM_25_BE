package com.freedom.news.domain.result;

import com.freedom.news.infra.client.response.NewsItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NewsClassificationResult {
    
    private final List<NewsItem> newNews;
    private final List<NewsItem> updatedNews;
    
    public static NewsClassificationResult of(List<NewsItem> newNews, List<NewsItem> updatedNews) {
        return NewsClassificationResult.builder()
                .newNews(newNews)
                .updatedNews(updatedNews)
                .build();
    }
    
    public boolean hasNewsToProcess() {
        return !newNews.isEmpty() || !updatedNews.isEmpty();
    }
}
