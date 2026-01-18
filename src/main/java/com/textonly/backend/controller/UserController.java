package com.textonly.backend.controller;

import com.textonly.backend.dto.UserProfileDTO;
import com.textonly.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long id) {
        UserProfileDTO profile = userService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileDTO profileDTO) {
        UserProfileDTO updated = userService.updateProfile(id, profileDTO);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        userService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfileDTO>> searchUsers(@RequestParam String query) {
        List<UserProfileDTO> results = userService.searchUsers(query);
        return ResponseEntity.ok(results);
    }
}
