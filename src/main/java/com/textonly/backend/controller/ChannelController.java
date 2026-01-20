package com.textonly.backend.controller;

import com.textonly.backend.auth.JwtTokenProvider;
import com.textonly.backend.model.Channel;
import com.textonly.backend.model.ChannelMessage;
import com.textonly.backend.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channels")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChannelController {

    @Autowired
    private ChannelService channelService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<Channel> createChannel(
            @RequestBody Map<String, Object> request) {
        Channel channel = channelService.createChannel(
            (String) request.get("name"),
            (String) request.get("channelType"),
            Long.parseLong(request.get("serverId").toString())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(channel);
    }

    @GetMapping("/server/{serverId}")
    public ResponseEntity<List<Channel>> getServerChannels(@PathVariable Long serverId) {
        List<Channel> channels = channelService.getServerChannels(serverId);
        return ResponseEntity.ok(channels);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ChannelMessage> sendMessage(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        Long userId = extractUserIdFromHeader(authHeader);
        ChannelMessage message = channelService.sendMessage(
            id,
            userId,
            request.get("content"),
            request.getOrDefault("messageType", "TEXT")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<ChannelMessage>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") int limit) {
        List<ChannelMessage> messages = channelService.getChannelMessages(id, limit);
        return ResponseEntity.ok(messages);
    }

    private Long extractUserIdFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new RuntimeException("Invalid Authorization header");
    }
}
