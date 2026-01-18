package com.textonly.backend.websocket;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyncMessage {
    private String type; // message.sent, profile.updated, user.status.changed, contact.added
    private Long senderId;
    private String content;
    private Object data;
    private Long timestamp;
}
