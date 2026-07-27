package com.Doctalk.Doctalk_backend.dto.request;

import jakarta.persistence.Column;

public record TagRequest(
        String name,
        String color
) {
}
