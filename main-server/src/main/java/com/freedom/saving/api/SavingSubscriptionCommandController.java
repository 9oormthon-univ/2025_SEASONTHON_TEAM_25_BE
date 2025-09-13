package com.freedom.saving.api;

import com.freedom.common.security.CustomUserPrincipal;
import com.freedom.saving.api.subscription.OpenSubscriptionRequest;
import com.freedom.saving.api.subscription.OpenSubscriptionResponse;
import com.freedom.saving.application.SavingSubscriptionCommandService;
import com.freedom.saving.application.signup.OpenSubscriptionCommand;
import com.freedom.saving.application.signup.OpenSubscriptionResult;
import com.freedom.saving.application.signup.SavingSubscriptionService;
import com.freedom.saving.application.MaturitySettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/savings/subscriptions")
@Validated
@RequiredArgsConstructor
public class SavingSubscriptionCommandController {

    private final SavingSubscriptionService service;
    private final SavingSubscriptionCommandService commandService;
    private final MaturitySettlementService maturitySettlementService;

    public record MaturityQuoteResponse(BigDecimal principal, BigDecimal rate, BigDecimal interest, BigDecimal total) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpenSubscriptionResponse open(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody @Validated OpenSubscriptionRequest req
    ) {
        var cmd = new OpenSubscriptionCommand(
                principal.getId(),
                req.productSnapshotId(),
                req.termMonths(),
                req.autoDebitAmount()
        );
        OpenSubscriptionResult r = service.open(cmd);
        return new OpenSubscriptionResponse(
                r.subscriptionId(),
                r.startDate(),
                r.maturityDate()
        );
    }

    @DeleteMapping("/{subscriptionId}")
    public org.springframework.http.ResponseEntity<?> cancel(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long subscriptionId
    ) {
        commandService.cancelByUser(principal.getId(), subscriptionId);
        return org.springframework.http.ResponseEntity.ok(com.freedom.common.exception.SuccessResponse.ok("적금 해지가 완료되었습니다."));
    }

    @GetMapping("/maturity/pending")
    public List<MaturitySettlementService.PendingMaturityDto> pendingMaturities(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return maturitySettlementService.listPendingMaturities(principal.getId());
    }

    @PostMapping("/{subscriptionId}/maturity/settlement")
    @ResponseStatus(HttpStatus.OK)
    public MaturityQuoteResponse claim(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long subscriptionId
    ) {
        var q = maturitySettlementService.settleMaturity(principal.getId(), subscriptionId);
        return new MaturityQuoteResponse(q.principal(), q.rate(), q.interest(), q.total());
    }
}
