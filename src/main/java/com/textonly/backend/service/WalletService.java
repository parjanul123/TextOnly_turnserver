package com.textonly.backend.service;

import com.textonly.backend.model.Transaction;
import com.textonly.backend.model.User;
import com.textonly.backend.model.UserWallet;
import com.textonly.backend.repository.TransactionRepository;
import com.textonly.backend.repository.UserRepository;
import com.textonly.backend.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserWalletRepository userWalletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public UserWallet getUserWallet(Long userId) {
        return userWalletRepository.findByUserId(userId)
                .orElseGet(() -> createWallet(userId));
    }

    @Transactional
    public UserWallet createWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserWallet wallet = UserWallet.builder()
                .user(user)
                .coins(0)
                .totalSpent(0)
                .totalEarned(0)
                .build();

        return userWalletRepository.save(wallet);
    }

    @Transactional
    public UserWallet addCoins(Long userId, Integer amount, String description) {
        if (amount <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        UserWallet wallet = getUserWallet(userId);
        wallet.setCoins(wallet.getCoins() + amount);
        wallet.setTotalEarned(wallet.getTotalEarned() + amount);
        userWalletRepository.save(wallet);

        // Create transaction record
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = Transaction.builder()
                .user(user)
                .transactionType(Transaction.TransactionType.TOP_UP)
                .amount(amount)
                .description(description != null ? description : "Coins added")
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        return wallet;
    }

    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
