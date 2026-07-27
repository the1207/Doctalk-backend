package com.Doctalk.Doctalk_backend.dto.reponse;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;

import java.time.LocalDateTime;

public record DocumentReponse(
        Long id,
        String title,
        String filename,
        String filePath,
        Long fileSize,
        String mimeType,
        String content,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
