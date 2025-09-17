package com.freedom.saving.domain.vo;

import com.freedom.common.exception.custom.InvalidTermException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가입 기간(개월) VO
 * 정책은 애플리케이션 서비스에서 주입 받지만, 도메인 기본 제약(1~60개월 등)은 최소선으로 방어
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermMonths {

    @Column(name = "term_months", nullable = false)
    private Integer value;

    public TermMonths(Integer months) {
        if (months == null || months < 1 || months > 60) {
            throw new InvalidTermException("가입 기간은 1~60개월이어야 합니다.");
        }
        this.value = months;
    }
}
