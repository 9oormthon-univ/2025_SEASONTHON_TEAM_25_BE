package com.freedom.saving.api.controller;

import com.freedom.common.security.CustomUserPrincipal;
import com.freedom.saving.application.subscription.SavingStatusQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.freedom.saving.application.subscription.SavingStatusQueryService.*;

@RestController
@RequestMapping("/api/savings/subscriptions")
@Validated
@RequiredArgsConstructor
public class SavingSubscriptionQueryController {

    private final SavingStatusQueryService service;

    @GetMapping("/active")
    public List<ActiveDto> getActive(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return service.getActive(principal.getId());
    }

    @GetMapping("/completed")
    public List<CompletedDto> getCompleted(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return service.getCompleted(principal.getId());
    }
}
