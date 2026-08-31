package com.Doctalk.Doctalk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "La question est obligatoire") String question,
        Long conversationId,
        Long documentId
) {}
