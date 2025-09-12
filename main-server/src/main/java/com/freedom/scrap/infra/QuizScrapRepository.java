package com.freedom.scrap.infra;

import com.freedom.scrap.domain.entity.QuizScrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizScrapRepository extends JpaRepository<QuizScrap, Long> {
    @Query("""
        SELECT qs FROM QuizScrap qs 
        JOIN FETCH qs.userQuiz uq
        JOIN FETCH uq.quiz q
        WHERE qs.user.id = :userId 
        ORDER BY qs.scrappedDate DESC, qs.createdAt DESC
        """)
    Page<QuizScrap> findByUserIdOrderByScrappedDateDesc(
        @Param("userId") Long userId, 
        Pageable pageable
    );
    
    Optional<QuizScrap> findByUserIdAndUserQuizId(Long userId, Long userQuizId);
}
