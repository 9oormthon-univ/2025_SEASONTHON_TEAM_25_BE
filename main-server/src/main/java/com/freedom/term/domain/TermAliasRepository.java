package com.freedom.term.domain;

import com.freedom.term.domain.entity.TermAlias;

import java.util.Optional;

// Port 인터페이스
public interface TermAliasRepository {

    Optional<TermAlias> findByAliasTerm(String aliasTerm);
}
