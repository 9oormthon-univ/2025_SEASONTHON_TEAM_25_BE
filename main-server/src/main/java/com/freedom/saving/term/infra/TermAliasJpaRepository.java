package com.freedom.saving.term.infra;

import com.freedom.saving.term.domain.TermAliasRepository;
import com.freedom.saving.term.domain.entity.TermAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermAliasJpaRepository extends JpaRepository<TermAlias, Long>, TermAliasRepository {

    @Override
    Optional<TermAlias> findByAliasTerm(String aliasTerm);
}
