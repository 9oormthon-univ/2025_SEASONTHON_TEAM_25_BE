package com.freedom.saving.api.controller;

import com.freedom.common.exception.SuccessResponse;
import com.freedom.common.logging.Loggable;
import com.freedom.common.security.CustomUserPrincipal;
import com.freedom.saving.api.dto.OpenSubscriptionRequest;
import com.freedom.saving.api.dto.OpenSubscriptionResponse;
import com.freedom.saving.application.subscription.SavingCancelService;
import com.freedom.saving.application.subscription.dto.OpenSubscriptionCommand;
import com.freedom.saving.application.subscription.dto.OpenSubscriptionResult;
import com.freedom.saving.application.subscription.SavingOpenService;
import com.freedom.saving.application.maturity.MaturitySettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/savings/subscriptions")
@Validated
@RequiredArgsConstructor
public class SavingSubscriptionCommandController {

    private final SavingOpenService service;
    private final SavingCancelService commandService;
    private final MaturitySettlementService maturitySettlementService;

    public record MaturityQuoteResponse(BigDecimal principal, BigDecimal rate, BigDecimal interest, BigDecimal total) {}

    @Loggable("적금 가입 API")
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
                r.maturityDate(),
                "적금 가입이 성공적으로 완료되었습니다."
        );
    }

    @Loggable("적금 해지 API")
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<?> cancel(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long subscriptionId
    ) {
        commandService.cancelByUser(principal.getId(), subscriptionId);
        return ResponseEntity.ok(SuccessResponse.ok("적금 해지가 완료되었습니다."));
    }


    @Loggable("만기 정산 API")
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
