package com.freedom.news.infra.repository;

import com.freedom.news.domain.entity.NewsReadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NewsHistoryRepository extends JpaRepository<NewsReadHistory, Long> {
    NewsReadHistory findByUserIdAndNewsArticleId(Long userId, Long newsArticleId);

    int countByUserIdAndReadAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
