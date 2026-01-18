package com.textonly.backend.service;

import com.textonly.backend.dto.UserProfileDTO;
import com.textonly.backend.model.User;
import com.textonly.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserProfileDTO getUserProfile(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        return mapToDTO(userOpt.get());
    }

    public UserProfileDTO updateProfile(Long userId, UserProfileDTO profileDTO) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        if (profileDTO.getDisplayName() != null) {
            user.setDisplayName(profileDTO.getDisplayName());
        }
        if (profileDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(profileDTO.getAvatarUrl());
        }
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    public void updateStatus(Long userId, String status) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus(status);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public List<UserProfileDTO> searchUsers(String displayName) {
        return userRepository.findByDisplayNameContainingIgnoreCase(displayName)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private UserProfileDTO mapToDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .build();
    }
}
