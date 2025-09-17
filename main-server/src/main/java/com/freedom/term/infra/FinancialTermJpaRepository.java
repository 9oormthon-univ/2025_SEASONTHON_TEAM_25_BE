package com.freedom.term.infra;

import com.freedom.term.domain.FinancialTermRepository;
import com.freedom.term.domain.entity.FinancialTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancialTermJpaRepository extends JpaRepository<FinancialTerm, Long>, FinancialTermRepository {

    @Override
    Optional<FinancialTerm> findByTerm(String term);
}
