package com.textonly.backend.service;

import com.textonly.backend.dto.UserProfileDTO;
import com.textonly.backend.model.Contact;
import com.textonly.backend.model.User;
import com.textonly.backend.repository.ContactRepository;
import com.textonly.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    public List<UserProfileDTO> getContacts(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        return contactRepository.findByUser(userOpt.get())
                .stream()
                .map(contact -> mapContactToDTO(contact.getContact()))
                .collect(Collectors.toList());
    }

    public UserProfileDTO addContact(Long userId, Long contactId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> contactOpt = userRepository.findById(contactId);

        if (userOpt.isEmpty() || contactOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // Check if already exists
        if (contactRepository.findByUserAndContact(userOpt.get(), contactOpt.get()).isPresent()) {
            throw new RuntimeException("Contact already exists");
        }

        Contact contact = Contact.builder()
                .user(userOpt.get())
                .contact(contactOpt.get())
                .createdAt(LocalDateTime.now())
                .build();

        contactRepository.save(contact);
        return mapContactToDTO(contactOpt.get());
    }

    public void removeContact(Long userId, Long contactId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> contactOpt = userRepository.findById(contactId);

        if (userOpt.isPresent() && contactOpt.isPresent()) {
            contactRepository.deleteByUserAndContact(userOpt.get(), contactOpt.get());
        }
    }

    private UserProfileDTO mapContactToDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .build();
    }
}
