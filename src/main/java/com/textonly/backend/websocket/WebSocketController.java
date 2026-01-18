package com.textonly.backend.websocket;

import com.textonly.backend.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle message sync between connected clients
     * Client sends: /app/chat/{receiverId}
     * Broadcast to: /topic/chat/{senderId}, /topic/chat/{receiverId}
     */
    @MessageMapping("/chat/{receiverId}")
    public void handleMessage(@DestinationVariable Long receiverId, SyncMessage message) {
        // Broadcast to sender
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getSenderId(),
                message
        );
        
        // Broadcast to receiver
        messagingTemplate.convertAndSend(
                "/topic/chat/" + receiverId,
                message
        );
    }

    /**
     * Handle profile update sync
     * Client sends: /app/user/{userId}/profile
     */
    @MessageMapping("/user/{userId}/profile")
    public void handleProfileUpdate(@DestinationVariable Long userId, SyncMessage message) {
        // Broadcast to all connected clients
        messagingTemplate.convertAndSend(
                "/topic/user/" + userId,
                message
        );
    }

    /**
     * Handle user status change
     * Client sends: /app/user/{userId}/status
     */
    @MessageMapping("/user/{userId}/status")
    public void handleStatusChange(@DestinationVariable Long userId, SyncMessage message) {
        // Broadcast to all connected clients
        messagingTemplate.convertAndSend(
                "/topic/users/status",
                message
        );
    }
}
