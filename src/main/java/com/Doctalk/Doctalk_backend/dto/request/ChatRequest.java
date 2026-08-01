package com.Doctalk.Doctalk_backend.dto.request;

public record ChatRequest(
        String question,
        Long conversationId  // Optionnel : si null, on crée une nouvelle conversation
) {}