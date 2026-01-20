package com.textonly.backend.service;

import com.textonly.backend.model.StoreItem;
import com.textonly.backend.model.Transaction;
import com.textonly.backend.model.User;
import com.textonly.backend.model.UserInventory;
import com.textonly.backend.model.UserWallet;
import com.textonly.backend.repository.StoreItemRepository;
import com.textonly.backend.repository.TransactionRepository;
import com.textonly.backend.repository.UserInventoryRepository;
import com.textonly.backend.repository.UserRepository;
import com.textonly.backend.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreItemRepository storeItemRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final UserWalletRepository userWalletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public List<StoreItem> getStoreItems(String itemType) {
        if (itemType != null && !itemType.isEmpty()) {
            try {
                StoreItem.ItemType type = StoreItem.ItemType.valueOf(itemType.toUpperCase());
                return storeItemRepository.findByItemType(type);
            } catch (IllegalArgumentException e) {
                return storeItemRepository.findAll();
            }
        }
        return storeItemRepository.findAll();
    }

    @Transactional
    public UserInventory purchaseItem(Long userId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StoreItem item = storeItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        UserWallet wallet = userWalletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserWallet newWallet = UserWallet.builder()
                            .user(user)
                            .coins(0)
                            .totalSpent(0)
                            .totalEarned(0)
                            .build();
                    return userWalletRepository.save(newWallet);
                });

        if (wallet.getCoins() < item.getPrice()) {
            throw new RuntimeException("Insufficient coins");
        }

        // Deduct coins
        wallet.setCoins(wallet.getCoins() - item.getPrice());
        wallet.setTotalSpent(wallet.getTotalSpent() + item.getPrice());
        userWalletRepository.save(wallet);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .user(user)
                .transactionType(Transaction.TransactionType.PURCHASE)
                .amount(item.getPrice())
                .description("Purchased " + item.getName())
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        // Add to inventory
        UserInventory inventory = UserInventory.builder()
                .user(user)
                .storeItem(item)
                .purchaseDate(LocalDateTime.now())
                .build();

        return userInventoryRepository.save(inventory);
    }

    public List<UserInventory> getUserInventory(Long userId) {
        return userInventoryRepository.findByUserId(userId);
    }
}
