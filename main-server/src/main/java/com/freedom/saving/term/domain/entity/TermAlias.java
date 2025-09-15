package com.freedom.saving.term.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "term_aliases")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alias_term", nullable = false, unique = true, length = 100)
    private String aliasTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private FinancialTerm financialTerm;

    @Builder
    public TermAlias(String aliasTerm) {
        this.aliasTerm = aliasTerm;
    }

}
