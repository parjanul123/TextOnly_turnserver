package com.textonly.backend.dto;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageCreateDTO {

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Message content cannot exceed 5000 characters")
    private String content;
}
