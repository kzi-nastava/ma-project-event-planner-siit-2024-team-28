package com.eventplanner.model.auth;

import java.util.UUID;

public class JwtPayload {
    private String role;
    private Long userId;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

