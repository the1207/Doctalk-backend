package com.Doctalk.Doctalk_backend.dto.reponse;

import jakarta.persistence.Column;

import java.time.LocalDateTime;

public record FolderReponse(
        Long id,
        String name,
        Long parentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
