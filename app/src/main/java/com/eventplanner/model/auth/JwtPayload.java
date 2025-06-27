package com.eventplanner.model.auth;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class JwtPayload {
    private String roles;
    private Long userId;

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<String> getRolesList() {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        String trimmed = roles.replaceAll("^\\[|\\]$", "").trim();

        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.asList(trimmed.split(",\\s*"));
    }
}

