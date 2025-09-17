package com.freedom.saving.api.controller;

import com.freedom.common.security.CustomUserPrincipal;
import com.freedom.saving.application.service.SavingSubscriptionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/savings/subscriptions")
@Validated
@RequiredArgsConstructor
public class SavingSubscriptionQueryController {

    private final SavingSubscriptionQueryService service;

    @GetMapping("/active")
    public List<SavingSubscriptionQueryService.ActiveDto> getActive(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return service.getActive(principal.getId());
    }

    @GetMapping("/completed")
    public List<SavingSubscriptionQueryService.CompletedDto> getCompleted(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return service.getCompleted(principal.getId());
    }
}
