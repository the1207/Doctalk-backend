package com.Doctalk.Doctalk_backend.dto.reponse;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationDetailResponse(
        Long id,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<MessageResponse> messages
) {}