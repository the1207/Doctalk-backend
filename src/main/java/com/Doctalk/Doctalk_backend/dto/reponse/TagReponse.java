package com.Doctalk.Doctalk_backend.dto.reponse;

import jakarta.persistence.Column;

import java.time.LocalDateTime;

public record TagReponse(
        Long id,
        String name,
        String color,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
