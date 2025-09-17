package com.freedom.term.infra;

import com.freedom.term.domain.TermAliasRepository;
import com.freedom.term.domain.entity.TermAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermAliasJpaRepository extends JpaRepository<TermAlias, Long>, TermAliasRepository {

    @Override
    Optional<TermAlias> findByAliasTerm(String aliasTerm);
}
