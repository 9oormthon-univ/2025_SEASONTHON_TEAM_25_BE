package com.freedom.term.domain;

import com.freedom.term.domain.entity.FinancialTerm;

import java.util.Optional;

// Port 인터페이스
public interface FinancialTermRepository {

    Optional<FinancialTerm> findByTerm(String term);
}
