package com.freedom.saving.term.api;

import com.freedom.saving.term.api.dto.TermResponseDto;
import com.freedom.saving.term.application.FinancialTermService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/savings/terms")
@RequiredArgsConstructor
public class FinancialTermController {

    private final FinancialTermService termService;

    @GetMapping("/{termName}")
    public ResponseEntity<TermResponseDto> getTerm(@PathVariable String termName) {
        TermResponseDto responseDto = termService.findTerm(termName);
        return ResponseEntity.ok(responseDto);
    }
}
