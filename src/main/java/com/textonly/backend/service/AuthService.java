package com.textonly.backend.service;

import com.textonly.backend.auth.JwtTokenProvider;
import com.textonly.backend.dto.AuthRequestDTO;
import com.textonly.backend.dto.AuthResponseDTO;
import com.textonly.backend.model.User;
import com.textonly.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public AuthResponseDTO register(AuthRequestDTO request) {
        // Check if user exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getEmail())
                .status("online")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT
        String token = jwtTokenProvider.generateToken(savedUser.getEmail(), savedUser.getId());

        return AuthResponseDTO.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .displayName(savedUser.getDisplayName())
                .avatarUrl(savedUser.getAvatarUrl())
                .status(savedUser.getStatus())
                .token(token)
                .tokenExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getExpirationTime() / 1000))
                .build();
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Update status to online
        user.setStatus("online");
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId());

        return AuthResponseDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .token(token)
                .tokenExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getExpirationTime() / 1000))
                .build();
    }

    public void logout(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus("offline");
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}
