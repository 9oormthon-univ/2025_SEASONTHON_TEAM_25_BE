package com.freedom.saving.term.domain;

import com.freedom.saving.term.domain.entity.FinancialTerm;

import java.util.Optional;

// Port 인터페이스
public interface FinancialTermRepository {

    Optional<FinancialTerm> findByTerm(String term);
}
