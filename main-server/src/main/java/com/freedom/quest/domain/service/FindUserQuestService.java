package com.freedom.quest.domain.service;

import com.freedom.quest.application.dto.QuestDto;
import com.freedom.quest.application.dto.UserQuestDto;
import com.freedom.quest.domain.entity.UserQuest;
import com.freedom.quest.infra.repository.UserQuestJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FindUserQuestService {

    private final UserQuestJPARepository userQuestRepository;

    @Transactional(readOnly = true)
    public List<UserQuestDto> findUserQuestById(Long id) {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = now.with(DayOfWeek.SUNDAY);

        List<UserQuest> userQuestList = userQuestRepository.findByUserIdAndPeriodOverlap(id, startOfWeek, endOfWeek);
        if (userQuestList.isEmpty()) {
            return List.of();
        }
        return userQuestList.stream()
                .map(UserQuestDto::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean checkQuestCompleted(Long userQuestId) {
        return userQuestRepository.findById(userQuestId)
                .map(uq -> uq.isCompleted() && !uq.isClaimed())
                .orElse(false);
    }
}
