package com.freedom.news.domain.service;

import com.freedom.news.infra.repository.NewsArticleRepository;
import com.freedom.common.util.HashUtil;
import com.freedom.news.application.dto.ExistingNewsDto;
import com.freedom.news.domain.result.NewsClassificationResult;
import com.freedom.news.infra.client.response.NewsItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsExistingCheckService {

    private final NewsArticleRepository newsArticleRepository;

    public NewsClassificationResult classifyNews(List<NewsItem> newsList) {
        List<String> newsItemIds = newsList.stream()
                .filter(news -> news.getGroupingCode().equals("policy"))
                .map(NewsItem::getNewsItemId)
                .toList();

        Map<String, ExistingNewsDto> existingNewsMap = getExistingNewsMap(newsItemIds);

        List<NewsItem> newNews = newsList.stream()
                .filter(newsItem -> !existingNewsMap.containsKey(newsItem.getNewsItemId()) && newsItem.getGroupingCode().equals("policy"))
                .toList();

        List<NewsItem> updatedNews = newsList.stream()
                .filter(newsItem -> {
                    ExistingNewsDto existing = existingNewsMap.get(newsItem.getNewsItemId());
                    if (existing == null || !newsItem.getGroupingCode().equals("policy")) return false;

                    boolean modified = newsItem.getModifyId() != null
                            && newsItem.getModifyId() > existing.getModifyId();

                    String newHash = HashUtil.sha256(newsItem.getDataContents());
                    boolean contentChanged = !newHash.equals(existing.getContentHash());

                    return modified || contentChanged;
                })
                .toList();

        return NewsClassificationResult.of(newNews, updatedNews);
    }

    private Map<String, ExistingNewsDto> getExistingNewsMap(List<String> newsItemIds) {
        List<ExistingNewsDto> existingNews =
                newsArticleRepository.findNewsItemIdAndModifyIdAndHashByNewsItemIdIn(newsItemIds);

        return existingNews.stream()
                .collect(Collectors.toMap(
                        ExistingNewsDto::getNewsItemId,
                        dto -> dto,
                        (existing, replacement) -> existing
                ));
    }
}
