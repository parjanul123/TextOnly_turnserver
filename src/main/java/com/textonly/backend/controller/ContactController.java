package com.textonly.backend.controller;

import com.textonly.backend.dto.UserProfileDTO;
import com.textonly.backend.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping
    public ResponseEntity<List<UserProfileDTO>> getContacts(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        List<UserProfileDTO> contacts = contactService.getContacts(userId);
        return ResponseEntity.ok(contacts);
    }

    @PostMapping("/{contactId}")
    public ResponseEntity<UserProfileDTO> addContact(
            @PathVariable Long contactId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        UserProfileDTO contact = contactService.addContact(userId, contactId);
        return ResponseEntity.status(HttpStatus.CREATED).body(contact);
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> removeContact(
            @PathVariable Long contactId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromHeader(authHeader);
        contactService.removeContact(userId, contactId);
        return ResponseEntity.noContent().build();
    }

    // Helper method - in production use Spring Security Principal
    private Long extractUserIdFromHeader(String authHeader) {
        // This is a placeholder - implement JWT extraction
        return 1L; // Default for now
    }
}
