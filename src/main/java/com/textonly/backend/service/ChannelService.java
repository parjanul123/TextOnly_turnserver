package com.textonly.backend.service;

import com.textonly.backend.model.Channel;
import com.textonly.backend.model.ChannelMessage;
import com.textonly.backend.model.Server;
import com.textonly.backend.model.User;
import com.textonly.backend.repository.ChannelMessageRepository;
import com.textonly.backend.repository.ChannelRepository;
import com.textonly.backend.repository.ServerRepository;
import com.textonly.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMessageRepository channelMessageRepository;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;

    @Transactional
    public Channel createChannel(String name, String channelType, Long serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));

        Channel.ChannelType type;
        try {
            type = Channel.ChannelType.valueOf(channelType.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = Channel.ChannelType.TEXT;
        }

        Channel channel = Channel.builder()
                .name(name)
                .type(type)
                .server(server)
                .createdAt(LocalDateTime.now())
                .build();

        return channelRepository.save(channel);
    }

    public List<Channel> getServerChannels(Long serverId) {
        return channelRepository.findByServerId(serverId);
    }

    @Transactional
    public ChannelMessage sendMessage(Long channelId, Long userId, String content, String messageType) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));
        
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChannelMessage.MessageType type;
        try {
            type = ChannelMessage.MessageType.valueOf(messageType.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = ChannelMessage.MessageType.TEXT;
        }

        ChannelMessage message = ChannelMessage.builder()
                .channel(channel)
                .sender(sender)
                .content(content)
                .messageType(type)
                .timestamp(LocalDateTime.now())
                .build();

        return channelMessageRepository.save(message);
    }

    public List<ChannelMessage> getChannelMessages(Long channelId, int limit) {
        if (limit <= 0 || limit > 100) {
            limit = 50;
        }
        return channelMessageRepository.findTop50ByChannelIdOrderByTimestampDesc(channelId)
                .stream()
                .limit(limit)
                .toList();
    }
}
