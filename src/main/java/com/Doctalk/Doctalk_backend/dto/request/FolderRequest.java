package com.Doctalk.Doctalk_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FolderRequest(
        @NotBlank(message = "Le nom est obligatoire") String name,
        Long parentId
) {
}
