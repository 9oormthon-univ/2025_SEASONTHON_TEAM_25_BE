package com.freedom.scrap.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
@Table(name = "scrap_history")
public class ScrapHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "scrap_id", nullable = false)
    private Long scrapId;

    @Enumerated(EnumType.STRING)
    private ScrapType type;

    private LocalDateTime scrapAt;

    public enum ScrapType {
        NEWS, QUIZ
    }

    @Builder
    public ScrapHistory(Long userId, Long scrapId, ScrapType type, LocalDateTime scrapAt) {
        this.userId = userId;
        this.scrapId = scrapId;
        this.type = type;
        this.scrapAt = scrapAt != null ? scrapAt : LocalDateTime.now();
    }
}
