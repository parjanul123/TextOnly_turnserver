package com.textonly.backend.repository;

import com.textonly.backend.model.ChannelMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChannelMessageRepository extends JpaRepository<ChannelMessage, Long> {
    List<ChannelMessage> findByChannelIdOrderByCreatedAtAsc(Long channelId);
    List<ChannelMessage> findTop50ByChannelIdOrderByCreatedAtDesc(Long channelId);
}
