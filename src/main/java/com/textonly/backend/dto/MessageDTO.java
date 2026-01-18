package com.textonly.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {

    private Long id;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
