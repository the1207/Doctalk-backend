package com.Doctalk.Doctalk_backend.security.dto;

import lombok.Builder;

import java.util.UUID;


@Builder
public class PasswordDTO {

    private String currentPassword;
    private String newPassword;
    private UUID userId;

    public PasswordDTO() {
    }

    public PasswordDTO(String currentPassword, String newPassword, UUID userId) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.userId = userId;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
