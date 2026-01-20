package com.textonly.backend.service;

import com.textonly.backend.model.Server;
import com.textonly.backend.model.User;
import com.textonly.backend.repository.ServerRepository;
import com.textonly.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServerService {

    private final ServerRepository serverRepository;
    private final UserRepository userRepository;

    @Transactional
    public Server createServer(String name, String description, String imageUrl, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Server server = Server.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .owner(owner)
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .build();

        return serverRepository.save(server);
    }

    public List<Server> getUserServers(Long userId) {
        return serverRepository.findByMembersId(userId);
    }

    public Server getServerById(Long serverId) {
        return serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));
    }

    @Transactional
    public void deleteServer(Long serverId, Long userId) {
        Server server = getServerById(serverId);
        if (!server.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only server owner can delete the server");
        }
        serverRepository.delete(server);
    }

    @Transactional
    public void addMember(Long serverId, Long userId) {
        Server server = getServerById(serverId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!server.getMembers().contains(user)) {
            server.getMembers().add(user);
            serverRepository.save(server);
        }
    }

    @Transactional
    public void removeMember(Long serverId, Long userId, Long requesterId) {
        Server server = getServerById(serverId);
        
        // Only owner or the user themselves can remove membership
        if (!server.getOwner().getId().equals(requesterId) && !userId.equals(requesterId)) {
            throw new RuntimeException("Unauthorized to remove member");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        server.getMembers().remove(user);
        serverRepository.save(server);
    }
}
