package com.freedom.news.infra.repository;

import com.freedom.news.domain.entity.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    @Query("SELECT na FROM NewsArticle na WHERE na.approveDate >= :startDate AND na.approveDate < :endDate ORDER BY na.approveDate DESC")
    Page<NewsArticle> findRecentNewsByApproveDateBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}
