package com.freedom.quiz.infra;

import com.freedom.quiz.domain.entity.UserQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserQuizRepository extends JpaRepository<UserQuiz, Long> {

    List<UserQuiz> findByUserIdAndQuizDate(Long userId, LocalDate quizDate);

    @Query("SELECT uq.quiz.id FROM UserQuiz uq WHERE uq.userId = :userId " +
           "AND uq.quizDate >= :startDate AND uq.quizDate <= :endDate")
    List<Long> findQuizIdsByUserIdAndDateRange(@Param("userId") Long userId, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT uq.quiz.id FROM UserQuiz uq WHERE uq.id = :userQuizId")
    Long findQuizIdByUserQuizId(@Param("userQuizId") Long userQuizId);
}
