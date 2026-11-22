package com.Doctalk.Doctalk_backend.security.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserDTO {
    private UUID publicId;
    private String username;
    private String role;
    private boolean enable;
    private LocalDateTime createdAt;

    public UserDTO() {}

    public UserDTO(UUID publicId, String username, String role, boolean enable, LocalDateTime createdAt) {
        this.publicId = publicId;
        this.username = username;
        this.role = role;
        this.enable = enable;
        this.createdAt = createdAt;
    }

    public UUID getPublicId() { return publicId; }
    public void setPublicId(UUID publicId) { this.publicId = publicId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnable() { return enable; }
    public void setEnable(boolean enable) { this.enable = enable; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}