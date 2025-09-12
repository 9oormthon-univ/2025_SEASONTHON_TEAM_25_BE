package com.freedom.scrap.infra;

import com.freedom.scrap.domain.entity.NewsScrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NewsScrapRepository extends JpaRepository<NewsScrap, Long> {
    
    Optional<NewsScrap> findByUserIdAndNewsArticleId(Long userId, Long newsArticleId);

    @Query("""
        SELECT ns FROM NewsScrap ns 
        JOIN FETCH ns.newsArticle na
        WHERE ns.user.id = :userId 
        ORDER BY ns.scrappedDate DESC, ns.createdAt DESC
        """)
    Page<NewsScrap> findByUserIdOrderByScrappedDateDesc(
        @Param("userId") Long userId, 
        Pageable pageable
    );
    
    boolean existsByUserIdAndNewsArticleId(Long userId, Long newsArticleId);
}
