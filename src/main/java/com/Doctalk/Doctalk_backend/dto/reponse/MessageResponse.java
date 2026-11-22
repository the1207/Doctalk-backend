package com.Doctalk.Doctalk_backend.dto.reponse;

import java.time.LocalDateTime;
import java.util.List;

public record MessageResponse(
        Long id,
        Long conversationId,
        String role,
        String content,
        List<Source> sources,
        LocalDateTime createdAt
) {}