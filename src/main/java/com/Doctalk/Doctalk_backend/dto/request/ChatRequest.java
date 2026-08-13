package com.Doctalk.Doctalk_backend.dto.request;

public record ChatRequest(
        String question,
        Long conversationId,
        Long documentId
) {}
