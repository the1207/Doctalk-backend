package com.Doctalk.Doctalk_backend.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;

import java.time.LocalDateTime;

public record DocumentRequest(
        String title,
        String filename,
        String filePath,
        Long fileSize,
        String mimeType,
        String content,
        String status
) {
}
