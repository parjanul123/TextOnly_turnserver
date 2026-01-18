package com.textonly.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    private Long id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String status;
}
