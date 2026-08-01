package com.Doctalk.Doctalk_backend.dto.reponse;

import java.time.LocalDateTime;
import java.util.Set;

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
        LocalDateTime updatedAt,
        Set<String> tagNames,
        Set<String> folderNames
        ) {
}
