package com.eventplanner.model.auth;

import java.util.Collections;
import java.util.List;

public class JwtPayload {
    private List<String> roles;
    private Long userId;

    public List<String> getRoles() {
        return roles != null ? roles : Collections.emptyList();
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<String> getRolesList() {
        return getRoles();
    }
}

