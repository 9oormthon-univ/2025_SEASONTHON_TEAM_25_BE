package com.freedom.wallet.infra;

import com.freedom.wallet.domain.UserWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 지갑 JPA Repository
 */
@Repository
public interface UserWalletJpaRepository extends JpaRepository<UserWallet, Long> {

    /**
     * 사용자 ID로 지갑 조회
     */
    Optional<UserWallet> findByUserId(Long userId);

    /**
     * 사용자 ID로 지갑 존재 여부 확인
     */
    boolean existsByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserWallet u where u.userId = :userId")
    Optional<UserWallet> findByUserIdForUpdate(Long userId);
}
