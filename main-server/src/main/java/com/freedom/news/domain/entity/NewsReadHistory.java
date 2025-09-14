package com.freedom.news.domain.entity;

import com.freedom.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "news_read_history")
public class NewsReadHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "news_article_id", nullable = false)
    private Long newsArticleId;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    @Builder
    public NewsReadHistory(Long userId, Long newsArticleId, LocalDateTime readAt) {
        this.userId = userId;
        this.newsArticleId = newsArticleId;
        this.readAt = readAt;
    }
}
