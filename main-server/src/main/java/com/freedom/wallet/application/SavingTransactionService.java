package com.freedom.wallet.application;

import com.freedom.common.exception.custom.InsufficientBalanceException;
import com.freedom.wallet.domain.UserWallet;
import com.freedom.wallet.domain.WalletTransaction;
import com.freedom.wallet.domain.WalletTransactionRepository;
import com.freedom.wallet.domain.UserWalletRepository;
import com.freedom.wallet.infra.WalletTransactionJpaAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 적금 거래 처리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SavingTransactionService {

    private final UserWalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletTransactionJpaAdapter transactionJpaAdapter;

    /**
     * 적금 가입 처리
     */
    public WalletTransaction processSavingJoin(Long userId, String requestId, BigDecimal amount, Long subscriptionId) {
        // 1. 멱등성 확인
        if (transactionRepository.findByRequestId(requestId).isPresent()) {
            throw new IllegalArgumentException("이미 처리된 요청입니다. requestId: " + requestId);
        }

        // 2. 사용자 지갑 조회
        UserWallet userWallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 지갑을 찾을 수 없습니다. userId: " + userId));

        // 3. 비관적 락으로 지갑 조회
        UserWallet wallet = transactionJpaAdapter.findWalletByIdWithLock(userWallet.getId())
                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다. ID: " + userWallet.getId()));
        
        // 4. 도메인 로직 실행
        try {
            wallet.withdraw(amount);
        } catch (IllegalArgumentException e) {
            throw new InsufficientBalanceException(wallet.getBalance(), amount);
        }
        walletRepository.save(wallet);
        
        // 5. 거래 이력 저장
        WalletTransaction transaction = WalletTransaction.createSavingJoin(wallet, requestId, amount, subscriptionId);
        return transactionRepository.save(transaction);
    }

    /**
     * 적금 해제 처리 (멱등성 보장)
     * @param userId 사용자 ID
     * @param requestId 요청 ID (멱등성 보장용)
     * @param amount 해제 금액
     * @param subscriptionId 적금 구독 ID
     * @return 생성된 거래 이력
     */
    public WalletTransaction processSavingCancel(Long userId, String requestId, BigDecimal amount, Long subscriptionId) {
        // 1. 멱등성 확인
        if (transactionRepository.findByRequestId(requestId).isPresent()) {
            throw new IllegalArgumentException("이미 처리된 요청입니다. requestId: " + requestId);
        }

        // 2. 사용자 지갑 조회
        UserWallet userWallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 지갑을 찾을 수 없습니다. userId: " + userId));

        // 3. 비관적 락으로 지갑 조회
        UserWallet wallet = transactionJpaAdapter.findWalletByIdWithLock(userWallet.getId())
                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다. ID: " + userWallet.getId()));
        
        // 4. 도메인 로직 실행
        wallet.deposit(amount);
        walletRepository.save(wallet);
        
        // 5. 거래 이력 저장
        WalletTransaction transaction = WalletTransaction.createSavingCancel(wallet, requestId, amount, subscriptionId);
        return transactionRepository.save(transaction);
    }

    /**
     * 적금 만기 처리 (멱등성 보장)
     * @param userId 사용자 ID
     * @param requestId 요청 ID (멱등성 보장용)
     * @param amount 만기 금액
     * @param subscriptionId 적금 구독 ID
     * @return 생성된 거래 이력
     */
    public WalletTransaction processSavingMaturity(Long userId, String requestId, BigDecimal amount, Long subscriptionId) {
        // 1. 멱등성 확인
        if (transactionRepository.findByRequestId(requestId).isPresent()) {
            throw new IllegalArgumentException("이미 처리된 요청입니다. requestId: " + requestId);
        }

        // 2. 사용자 지갑 조회
        UserWallet userWallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 지갑을 찾을 수 없습니다. userId: " + userId));

        // 3. 비관적 락으로 지갑 조회
        UserWallet wallet = transactionJpaAdapter.findWalletByIdWithLock(userWallet.getId())
                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다. ID: " + userWallet.getId()));
        
        // 4. 도메인 로직 실행
        wallet.deposit(amount);
        walletRepository.save(wallet);
        
        // 5. 거래 이력 저장
        WalletTransaction transaction = WalletTransaction.createSavingMaturity(wallet, requestId, amount, subscriptionId);
        return transactionRepository.save(transaction);
    }

    /**
     * 적금 이자 처리 (멱등성 보장)
     * @param userId 사용자 ID
     * @param requestId 요청 ID (멱등성 보장용)
     * @param amount 이자 금액
     * @param subscriptionId 적금 구독 ID
     * @return 생성된 거래 이력
     */
    public WalletTransaction processSavingInterest(Long userId, String requestId, BigDecimal amount, Long subscriptionId) {
        // 1. 멱등성 확인
        if (transactionRepository.findByRequestId(requestId).isPresent()) {
            throw new IllegalArgumentException("이미 처리된 요청입니다. requestId: " + requestId);
        }

        // 2. 사용자 지갑 조회
        UserWallet userWallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 지갑을 찾을 수 없습니다. userId: " + userId));

        // 3. 비관적 락으로 지갑 조회
        UserWallet wallet = transactionJpaAdapter.findWalletByIdWithLock(userWallet.getId())
                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다. ID: " + userWallet.getId()));
        
        // 4. 도메인 로직 실행
        wallet.deposit(amount);
        walletRepository.save(wallet);
        
        // 5. 거래 이력 저장
        WalletTransaction transaction = WalletTransaction.createSavingInterest(wallet, requestId, amount, subscriptionId);
        return transactionRepository.save(transaction);
    }

    /**
     * 적금 자동 납입 처리 (멱등성 보장)
     */
    public WalletTransaction processSavingAutoDebit(Long userId, String requestId, BigDecimal amount, Long subscriptionId) {
        if (transactionRepository.findByRequestId(requestId).isPresent()) {
            return transactionRepository.findByRequestId(requestId).get();
        }
        UserWallet userWallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 지갑을 찾을 수 없습니다. userId: " + userId));
        UserWallet wallet = transactionJpaAdapter.findWalletByIdWithLock(userWallet.getId())
                .orElseThrow(() -> new IllegalArgumentException("지갑을 찾을 수 없습니다. ID: " + userWallet.getId()));

        // 출금(잔액 부족 시 도메인에서 예외 발생)
        try {
            wallet.withdraw(amount);
        } catch (IllegalArgumentException e) {
            throw new InsufficientBalanceException(wallet.getBalance(), amount);
        }
        walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.createSavingAutoDebit(wallet, requestId, amount, subscriptionId);
        return transactionRepository.save(transaction);
    }

    /**
     * 요청 ID 생성 (UUID 기반)
     * @return 고유한 요청 ID
     */
    public String generateRequestId() {
        return "SAVING_" + UUID.randomUUID().toString();
    }
}
