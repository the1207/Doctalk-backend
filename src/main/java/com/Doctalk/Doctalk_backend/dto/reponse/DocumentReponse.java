package com.Doctalk.Doctalk_backend.dto.reponse;

public record DocumentReponse(
        Long id,
        String title,
        String filename,
        String filePath,
        Long fileSize, String mimeType,
        String content,
        String status,

        java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt) {
}
