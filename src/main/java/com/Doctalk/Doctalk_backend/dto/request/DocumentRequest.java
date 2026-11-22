package com.Doctalk.Doctalk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DocumentRequest(
        @NotBlank(message = "Le titre est obligatoire") String title,
        String filename,
        String filePath,
        Long fileSize,
        String mimeType,
        String content,
        String status
) {
}
