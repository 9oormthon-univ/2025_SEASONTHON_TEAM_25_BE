package com.freedom.quest.infra.repository;

import com.freedom.quest.domain.entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestJPARepository extends JpaRepository<Quest, Long> {
    List<Quest> findAllByIsActiveTrue();
}
