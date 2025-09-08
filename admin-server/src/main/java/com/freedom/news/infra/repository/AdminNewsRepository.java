package com.freedom.news.infra.repository;

import com.freedom.news.domain.entity.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminNewsRepository extends JpaRepository<NewsArticle, Long> {

    Page<NewsArticle> findAllByOrderByApproveDateDesc(Pageable pageable);

    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.contentBlocks WHERE n.id = :newsId")
    Optional<NewsArticle> findByIdWithContentBlocks(Long newsId);
}
