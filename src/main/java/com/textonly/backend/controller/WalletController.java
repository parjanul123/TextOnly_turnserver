package com.textonly.backend.controller;

import com.textonly.backend.auth.JwtTokenProvider;
import com.textonly.backend.model.Transaction;
import com.textonly.backend.model.UserWallet;
import com.textonly.backend.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WalletController {

    @Autowired
    private WalletService walletService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<UserWallet> getWallet(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        UserWallet wallet = walletService.getUserWallet(userId);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/add")
    public ResponseEntity<UserWallet> addCoins(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        Long userId = extractUserIdFromHeader(authHeader);
        Integer amount = Integer.parseInt(request.get("amount").toString());
        String description = (String) request.get("description");
        UserWallet wallet = walletService.addCoins(userId, amount, description);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        List<Transaction> transactions = walletService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    private Long extractUserIdFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new RuntimeException("Invalid Authorization header");
    }
}
