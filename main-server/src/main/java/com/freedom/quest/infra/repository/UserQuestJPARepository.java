package com.freedom.quest.infra.repository;

import com.freedom.quest.domain.entity.UserQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserQuestJPARepository extends JpaRepository<UserQuest, Long> {

    @Query("SELECT uq FROM UserQuest uq WHERE uq.user.id = :userId " +
           "AND uq.periodStartDate <= :endDate AND uq.periodEndDate >= :startDate")
    List<UserQuest> findByUserIdAndPeriodOverlap(@Param("userId") Long userId, 
                                                  @Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);

}
