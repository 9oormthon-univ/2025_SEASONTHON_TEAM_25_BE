package com.freedom.term.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 금융 용어의 대표 개념을 정의하는 엔티티
 */
@Getter
@Entity
@Table(name = "financial_terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinancialTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "term", nullable = false, unique = true, length = 100)
    private String term; // 대표 용어

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description; // 용어 설명

    @Builder
    public FinancialTerm(String term, String description) {
        this.term = term;
        this.description = description;
    }
}
