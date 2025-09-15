package com.freedom.achievement.api;

import com.freedom.achievement.api.response.AchievementListResponse;
import com.freedom.achievement.api.response.ClaimAchievementResponse;
import com.freedom.achievement.application.AchievementAppService;
import com.freedom.common.logging.Loggable;
import com.freedom.common.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementAppService achievementAppService;

    @Loggable("사용자 업적 목록 조회 API")
    @GetMapping
    public ResponseEntity<AchievementListResponse> getAchievements(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        AchievementListResponse response = achievementAppService.getUserAchievements(principal.getId());
        return ResponseEntity.ok(response);
    }

    @Loggable("업적 확인 처리 API")
    @PostMapping("/{achievementId}/claim")
    public ResponseEntity<ClaimAchievementResponse> claimAchievement(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long achievementId) {

        ClaimAchievementResponse response = achievementAppService.claimAchievement(
                principal.getId(),
                achievementId
        );

        return ResponseEntity.ok(response);
    }
}
