package com.Doctalk.Doctalk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TagRequest(
        @NotBlank(message = "Le nom est obligatoire") String name,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Couleur au format hexadécimal attendu") String color
) {
}
