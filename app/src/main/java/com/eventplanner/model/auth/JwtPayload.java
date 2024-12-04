package com.eventplanner.model.auth;

import java.util.UUID;

public class JwtPayload {
    private String role;
    private UUID userId;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}

