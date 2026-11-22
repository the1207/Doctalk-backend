package com.Doctalk.Doctalk_backend.security.dto;

import java.util.UUID;

public class AuthentificationResponse {
    private String token;
    private UUID publicId;
    private String username;
    private String role;

    public AuthentificationResponse() {}

    public AuthentificationResponse(String token, UUID publicId, String username, String role) {
        this.token = token;
        this.publicId = publicId;
        this.username = username;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UUID getPublicId() { return publicId; }
    public void setPublicId(UUID publicId) { this.publicId = publicId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}