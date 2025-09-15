package com.freedom.saving.term.application;

import com.freedom.common.exception.custom.ResourceNotFoundException;
import com.freedom.saving.term.api.dto.TermResponseDto;
import com.freedom.saving.term.domain.FinancialTermRepository;
import com.freedom.saving.term.domain.TermAliasRepository;
import com.freedom.saving.term.domain.entity.FinancialTerm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialTermService {

    private final FinancialTermRepository termRepository;
    private final TermAliasRepository aliasRepository;

    public TermResponseDto findTerm(String termName) {
        return termRepository.findByTerm(termName)
                .map(term -> new TermResponseDto(term.getTerm(), term.getDescription()))
                .orElseGet(() -> findByAlias(termName));
    }

    private TermResponseDto findByAlias(String aliasName) {
        return aliasRepository.findByAliasTerm(aliasName)
                .map(alias -> {
                    FinancialTerm originalTerm = alias.getFinancialTerm();
                    return new TermResponseDto(originalTerm.getTerm(), originalTerm.getDescription());
                })
                .orElseThrow(() -> new ResourceNotFoundException("금융 용어를 찾을 수 없습니다: " + aliasName));
    }
}
