package com.freedom.quest.application;


import com.freedom.attendance.domain.service.AttendanceReadService;
import com.freedom.common.exception.custom.UnsupportedQuestTypeException;
import com.freedom.news.domain.service.FindNewsService;
import com.freedom.quest.api.response.ClaimResponse;
import com.freedom.quest.api.response.QuestSummaryResponse;
import com.freedom.quest.application.dto.UserQuestDto;
import com.freedom.quest.domain.service.FindUserQuestService;
import com.freedom.quest.domain.service.UserQuestCommandService;
import com.freedom.quiz.domain.service.FindUserQuizService;
import com.freedom.scrap.domain.service.FindScrapHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestFacade {

    private final FindUserQuestService findUserQuestService;
    private final UserQuestCommandService userQuestCommandService;
    private final AttendanceReadService attendanceReadService;
    private final FindNewsService findNewsService;
    private final FindUserQuizService findUserQuizService;
    private final FindScrapHistoryService findScrapHistoryService;

    public List<QuestSummaryResponse> fetchAndUpdateCurrentWeek(Long userId) {
        List<UserQuestDto> userQuestList = findUserQuestService.findUserQuestById(userId);

        if (userQuestList.isEmpty()) {
            List<UserQuestDto> updateUserQuests = asyncUpdateUserQuests(userQuestCommandService.saveUserQuest(userId), userId);
            return mapToQuestSummaryResponses(updateUserQuests);
        }

        List<UserQuestDto> updateUserQuests = asyncUpdateUserQuests(userQuestList, userId);
        return mapToQuestSummaryResponses(updateUserQuests);
    }

    private List<UserQuestDto> asyncUpdateUserQuests(List<UserQuestDto> userQuests, Long userId) {
        Map<Boolean, List<UserQuestDto>> partitioned = userQuests.stream()
                .collect(Collectors.partitioningBy(UserQuestDto::isCompleted));

        List<UserQuestDto> completedQuests = partitioned.get(true);
        List<UserQuestDto> incompleteQuests = partitioned.get(false);

        List<CompletableFuture<UserQuestDto>> futures = incompleteQuests.stream()
                .map(uq -> CompletableFuture.supplyAsync(() -> updateSingleQuest(uq, userId)))
                .toList();

        List<UserQuestDto> updatedQuests = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        List<UserQuestDto> result = new ArrayList<>(completedQuests);
        result.addAll(updatedQuests);
        return result;
    }

    private UserQuestDto updateSingleQuest(UserQuestDto uq, Long userId) {
        return switch(uq.getQuest().getTargetType()) {
            case "ATTENDANCE" -> {
                boolean attendanceCheck = attendanceReadService.wasAttendedYesterday(userId);
                yield userQuestCommandService.updateAttendanceQuest(uq.getUserQuestId(), attendanceCheck, uq.getQuest().getRequirementCount());
            }
            case "NEWS" -> {
                int newsHistoryCount = findNewsService.findNewsHistoryCountByUserId(userId);
                yield userQuestCommandService.updateNewsQuest(uq.getUserQuestId(), newsHistoryCount, uq.getQuest().getRequirementCount());
            }
            case "QUIZ_CORRECT_ONLY" -> {
                int quizHistoryCount = findUserQuizService.findQuizHistoryCountByUserId(userId);
                yield userQuestCommandService.updateQuizQuest(uq.getUserQuestId(), quizHistoryCount, uq.getQuest().getRequirementCount());
            }
            case "SCRAP_NEWS" -> {
                int newsScrapCount = findScrapHistoryService.findScrapHistoryCountByUserId(userId, "NEWS");
                yield userQuestCommandService.updateScrapQuest(uq.getUserQuestId(), newsScrapCount, uq.getQuest().getRequirementCount());
            }
            case "SCRAP_QUIZ" -> {
                int quizScrapCount = findScrapHistoryService.findScrapHistoryCountByUserId(userId, "QUIZ");
                yield userQuestCommandService.updateScrapQuest(uq.getUserQuestId(), quizScrapCount, uq.getQuest().getRequirementCount());
            }
            /* case "FINANCE_PRODUCT" -> {
                // TODO: 추후 추가 구현
            }*/
            default -> throw new UnsupportedQuestTypeException();
        };
    }


    private List<QuestSummaryResponse> mapToQuestSummaryResponses(List<UserQuestDto> userQuests){
        return userQuests.stream()
                .map(uq -> QuestSummaryResponse.builder()
                        .userQuestId(uq.getUserQuestId())
                        .title(uq.getQuest().getTitle())
                        .description(uq.getQuest().getDescription())
                        .periodStart(uq.getPeriodStartDate())
                        .periodEnd(uq.getPeriodEndDate())
                        .requirementCount(uq.getQuest().getRequirementCount())
                        .progressCount(uq.getProgressCount())
                        .currentStreak(uq.getCurrentStreak())
                        .completed(uq.isCompleted())
                        .claimed(uq.isClaimed())
                        .rewardAmount(uq.getQuest().getRewardAmount())
                        .build()
                ).toList();
    }

    public ClaimResponse questClaim(Long userId, Long userQuestId) {
        if(findUserQuestService.checkQuestCompleted(userQuestId)){
            userQuestCommandService.claimCompleted(userId, userQuestId);
            return ClaimResponse.success(userQuestId);
        } else{
            return ClaimResponse.alreadyClaimed(userQuestId);
        }
    }
}
