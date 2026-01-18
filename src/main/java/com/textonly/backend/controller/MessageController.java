package com.textonly.backend.controller;

import com.textonly.backend.dto.MessageCreateDTO;
import com.textonly.backend.dto.MessageDTO;
import com.textonly.backend.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody MessageCreateDTO request) {
        // Extract userId from JWT/Principal (in real app use Spring Security)
        Long senderId = extractUserIdFromHeader(authHeader);
        MessageDTO message = messageService.sendMessage(senderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<MessageDTO>> getConversation(
            @PathVariable Long otherUserId,
            @RequestHeader("Authorization") String authHeader) {
        Long currentUserId = extractUserIdFromHeader(authHeader);
        List<MessageDTO> messages = messageService.getConversation(currentUserId, otherUserId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        List<MessageDTO> messages = messageService.getUnreadMessages(userId);
        return ResponseEntity.ok(messages);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    // Helper method - in production use Spring Security Principal
    private Long extractUserIdFromHeader(String authHeader) {
        // This is a placeholder - implement JWT extraction with JwtTokenProvider
        return 1L; // Default for now
    }
}
