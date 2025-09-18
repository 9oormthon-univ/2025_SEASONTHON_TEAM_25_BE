package com.freedom.quest.domain.service;

import com.freedom.auth.domain.User;
import com.freedom.auth.infra.UserJpaRepository;
import com.freedom.common.exception.custom.*;
import com.freedom.quest.application.dto.UserQuestDto;
import com.freedom.quest.domain.entity.Quest;
import com.freedom.quest.domain.entity.TargetType;
import com.freedom.quest.domain.entity.UserQuest;
import com.freedom.quest.infra.repository.QuestJPARepository;
import com.freedom.quest.infra.repository.UserQuestJPARepository;
import com.freedom.wallet.domain.UserWallet;
import com.freedom.wallet.domain.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class UserQuestCommandService {

    private final UserQuestJPARepository userQuestRepository;
    private final QuestJPARepository questRepository;
    private final UserJpaRepository userRepository;
    private final UserWalletRepository userWalletRepository;

    @Transactional
    public List<UserQuestDto> saveUserQuest(long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = now.with(DayOfWeek.SUNDAY);

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        List<Quest> questList = questRepository.findAllByActiveTrue();
        List<UserQuest> userQuestList = questList.stream()
                .filter(quest -> {
                    if (quest.getTargetType() != TargetType.SCRAP_NEWS && quest.getTargetType() != TargetType.SCRAP_QUIZ) {
                        return true;
                    }
                    int week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    if (week % 2 == 1) {
                        return quest.getTargetType() == TargetType.SCRAP_NEWS;
                    } else {
                        return quest.getTargetType() == TargetType.SCRAP_QUIZ;
                    }
                })
                .map(quest -> UserQuest.builder()
                        .user(user)
                        .quest(quest)
                        .periodStartDate(startOfWeek)
                        .periodEndDate(endOfWeek)
                        .progressCount(0)
                        .currentStreak(0)
                        .build()
                ).toList();
        List<UserQuest> savedUserQuestList = userQuestRepository.saveAll(userQuestList);

        return savedUserQuestList.stream()
                .map(UserQuestDto::toDto)
                .toList();
    }

    @Transactional
    public UserQuestDto updateAttendanceQuest(Long userQuestId, int consecutiveDays, int requirementCount) {
        return updateQuestProgress(
                userQuestId,
                requirementCount,
                (userQuest) -> {
                    userQuest.updateCurrentStreak(consecutiveDays);
                    userQuest.updateProgressCount(requirementCount, consecutiveDays);
                }
        );
    }

    @Transactional
    public UserQuestDto updateNewsQuest(Long userQuestId, int newsHistoryCount, int requirementCount) {
        return updateQuestProgress(
                userQuestId,
                requirementCount,
                (userQuest) -> userQuest.updateProgressCount(requirementCount, newsHistoryCount)
        );
    }

    @Transactional
    public UserQuestDto updateQuizQuest(Long userQuestId, int quizHistoryCount, int requirementCount) {
        return updateQuestProgress(
                userQuestId,
                requirementCount,
                (userQuest) -> userQuest.updateProgressCount(requirementCount, quizHistoryCount)
        );
    }

    @Transactional
    public UserQuestDto updateScrapQuest(Long userQuestId, int newsScrapCount, int requirementCount) {
        return updateQuestProgress(
                userQuestId,
                requirementCount,
                (userQuest) -> userQuest.updateProgressCount(requirementCount, newsScrapCount)
        );
    }

    private UserQuestDto updateQuestProgress(Long userQuestId, int requirementCount, Consumer<UserQuest> updater) {
        UserQuest userQuest = userQuestRepository.findById(userQuestId)
                .orElseThrow(() -> new UserQuestNotFoundException(userQuestId));
        updater.accept(userQuest);

        if ((userQuest.getProgressCount() >= requirementCount) || (userQuest.getCurrentStreak() == requirementCount)) {
            userQuest.completeQuest();
        }
        UserQuest updatedUserQuest = userQuestRepository.save(userQuest);
        return UserQuestDto.toDto(updatedUserQuest);
    }

    @Transactional
    public void claimCompleted(Long userId, Long userQuestId) {
        UserWallet wallet = userWalletRepository.findByUserIdForUpdate(userId).orElseThrow(() -> new UserWalletNotFoundException(userId));
        UserQuest userQuest = userQuestRepository.findById(userQuestId)
                .orElseThrow(() -> new UserQuestNotFoundException(userQuestId));

        User user = userQuest.getUser();
        if (!user.getId().equals(userId)) {
            throw new QuestAccessDeniedException(userQuestId);
        }

        wallet.deposit(userQuest.getQuest().getRewardAmount());
        userWalletRepository.save(wallet);

        userQuest.claimReward();
        userQuestRepository.save(userQuest);
    }
}
