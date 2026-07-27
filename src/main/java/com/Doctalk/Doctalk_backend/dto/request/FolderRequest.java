package com.Doctalk.Doctalk_backend.dto.request;

import jakarta.persistence.Column;

import java.time.LocalDateTime;

public record FolderRequest(
        String name,
        Long parentId
) {
}
