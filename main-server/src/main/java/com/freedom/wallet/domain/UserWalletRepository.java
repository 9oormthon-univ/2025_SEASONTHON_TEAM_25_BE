package com.freedom.wallet.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 사용자 지갑 도메인 레벨 추상화
 * infra(JPA) 구현체는 wallet/infra 하위에 별도 작성
 */
public interface UserWalletRepository {

    /**
     * 지갑 저장
     */
    UserWallet save(UserWallet wallet);

    /**
     * ID로 지갑 조회
     */
    Optional<UserWallet> findById(Long id);

    /**
     * 사용자 ID로 지갑 조회
     */
    Optional<UserWallet> findByUserId(Long userId);

    /**
     * 사용자 ID로 지갑 존재 여부 확인
     */
    boolean existsByUserId(Long userId);

    /**
     * 지갑 삭제
     */
    void delete(UserWallet wallet);

    Optional<UserWallet> findByUserIdForUpdate(Long userId);
}
