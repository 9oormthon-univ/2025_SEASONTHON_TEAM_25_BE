package com.freedom.wallet.infra;

import com.freedom.wallet.domain.UserWallet;
import com.freedom.wallet.domain.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * UserWalletRepository JPA 구현체
 */
@Component
@RequiredArgsConstructor
public class UserWalletJpaAdapter implements UserWalletRepository {

    private final UserWalletJpaRepository jpaRepository;

    @Override
    public UserWallet save(UserWallet wallet) {
        return jpaRepository.save(wallet);
    }

    @Override
    public Optional<UserWallet> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<UserWallet> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public void delete(UserWallet wallet) {
        jpaRepository.delete(wallet);
    }

    @Override
    public Optional<UserWallet> findByUserIdForUpdate(Long userId) {
        return jpaRepository.findByUserIdForUpdate(userId);
    }
}
