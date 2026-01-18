package com.textonly.backend.service;

import com.textonly.backend.dto.MessageCreateDTO;
import com.textonly.backend.dto.MessageDTO;
import com.textonly.backend.model.Message;
import com.textonly.backend.model.User;
import com.textonly.backend.repository.MessageRepository;
import com.textonly.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public MessageDTO sendMessage(Long senderId, MessageCreateDTO request) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(request.getReceiverId());

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Message message = Message.builder()
                .sender(senderOpt.get())
                .receiver(receiverOpt.get())
                .content(request.getContent())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Message savedMessage = messageRepository.save(message);
        return mapToDTO(savedMessage);
    }

    public List<MessageDTO> getConversation(Long userId1, Long userId2) {
        return messageRepository.findConversationBetween(userId1, userId2)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getUnreadMessages(Long userId) {
        return messageRepository.findUnreadMessages(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void markAsRead(Long messageId) {
        Optional<Message> msgOpt = messageRepository.findById(messageId);
        if (msgOpt.isPresent()) {
            Message message = msgOpt.get();
            message.setIsRead(true);
            messageRepository.save(message);
        }
    }

    private MessageDTO mapToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getDisplayName())
                .receiverId(message.getReceiver().getId())
                .content(message.getContent())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
