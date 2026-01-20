package com.textonly.backend.controller;

import com.textonly.backend.auth.JwtTokenProvider;
import com.textonly.backend.model.StoreItem;
import com.textonly.backend.model.UserInventory;
import com.textonly.backend.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/store")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StoreController {

    @Autowired
    private StoreService storeService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/items")
    public ResponseEntity<List<StoreItem>> getStoreItems(
            @RequestParam(required = false) String type) {
        List<StoreItem> items = storeService.getStoreItems(type);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/buy")
    public ResponseEntity<UserInventory> buyItem(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Long> request) {
        Long userId = extractUserIdFromHeader(authHeader);
        UserInventory purchase = storeService.purchaseItem(userId, request.get("itemId"));
        return ResponseEntity.status(HttpStatus.CREATED).body(purchase);
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<UserInventory>> getInventory(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        List<UserInventory> inventory = storeService.getUserInventory(userId);
        return ResponseEntity.ok(inventory);
    }

    private Long extractUserIdFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new RuntimeException("Invalid Authorization header");
    }
}
