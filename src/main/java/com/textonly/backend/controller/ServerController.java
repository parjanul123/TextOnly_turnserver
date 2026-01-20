package com.textonly.backend.controller;

import com.textonly.backend.auth.JwtTokenProvider;
import com.textonly.backend.model.Server;
import com.textonly.backend.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ServerController {

    @Autowired
    private ServerService serverService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<Server> createServer(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        Long userId = extractUserIdFromHeader(authHeader);
        Server server = serverService.createServer(
            request.get("name"),
            request.get("description"),
            request.get("imageUrl"),
            userId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(server);
    }

    @GetMapping
    public ResponseEntity<List<Server>> getUserServers(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        List<Server> servers = serverService.getUserServers(userId);
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Server> getServer(@PathVariable Long id) {
        Server server = serverService.getServerById(id);
        return ResponseEntity.ok(server);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServer(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        serverService.deleteServer(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> addMember(
            @PathVariable Long id,
            @PathVariable Long userId) {
        serverService.addMember(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        Long requesterId = extractUserIdFromHeader(authHeader);
        serverService.removeMember(id, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserIdFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new RuntimeException("Invalid Authorization header");
    }
}
