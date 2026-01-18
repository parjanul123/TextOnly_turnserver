package com.textonly.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private Long userId;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String status;
    private String token; // JWT token
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;
}
