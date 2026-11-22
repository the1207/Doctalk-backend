package com.Doctalk.Doctalk_backend.dto.reponse;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int messageCount
) {}