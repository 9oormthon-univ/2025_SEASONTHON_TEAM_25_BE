package com.freedom.saving.term.infra;

import com.freedom.saving.term.domain.FinancialTermRepository;
import com.freedom.saving.term.domain.entity.FinancialTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancialTermJpaRepository extends JpaRepository<FinancialTerm, Long>, FinancialTermRepository {

    @Override
    Optional<FinancialTerm> findByTerm(String term);
}
