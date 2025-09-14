package com.freedom.quest.api;

import com.freedom.common.logging.Loggable;
import com.freedom.common.security.CustomUserPrincipal;
import com.freedom.quest.api.response.ClaimResponse;
import com.freedom.quest.api.response.QuestSummaryResponse;
import com.freedom.quest.application.QuestFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quests")
public class QuestController {

    private final QuestFacade questFacade;

    @Loggable("퀘스트 메인 조회")
    @GetMapping("/current")
    public ResponseEntity<List<QuestSummaryResponse>> getCurrentWeekQuests(@AuthenticationPrincipal CustomUserPrincipal principal) {
        List<QuestSummaryResponse> response = questFacade.fetchAndUpdateCurrentWeek(principal.getId());
        return ResponseEntity.ok(response);
    }

    @Loggable("퀘스트 보상 수령")
    @PostMapping("/{userQuestId}/claim")
    public ResponseEntity<ClaimResponse> claim(@AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable Long userQuestId) {
        ClaimResponse response = questFacade.questClaim(principal.getId(), userQuestId);
        return ResponseEntity.ok(response);
    }
}
