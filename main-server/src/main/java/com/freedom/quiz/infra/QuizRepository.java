package com.freedom.quiz.infra;

import com.freedom.quiz.domain.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("SELECT q FROM Quiz q WHERE q.category = 'news' " +
           "AND q.createdAt >= :weekStart AND q.createdAt <= :weekEnd " +
           "AND q.id NOT IN :excludeQuizIds ")
    List<Quiz> findNewsQuizzesInWeek(@Param("weekStart") LocalDateTime weekStart,
                                     @Param("weekEnd") LocalDateTime weekEnd,
                                     @Param("excludeQuizIds") List<Long> excludeQuizIds);

    @Query(value = "SELECT * FROM quiz q WHERE q.category = 'quiz' " +
           "AND q.id NOT IN :excludeQuizIds " +
           "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Quiz> findGeneralQuizzes(@Param("excludeQuizIds") List<Long> excludeQuizIds, 
                                 @Param("limit") int limit);
}
