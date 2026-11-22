package com.Doctalk.Doctalk_backend.dto.reponse;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponse(
        Long conversationId,
        String answer,
        List<Source> sources,
        LocalDateTime timestamp
) {}
