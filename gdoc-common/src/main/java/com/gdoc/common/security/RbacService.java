package com.gdoc.common.security;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RbacService {

    private static final Map<String, Set<String>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        ROLE_PERMISSIONS.put("admin", Set.of(
            "doc:create", "doc:read", "doc:update", "doc:delete",
            "doc:share", "doc:export", "doc:import",
            "user:manage", "team:manage", "template:manage",
            "comment:create", "comment:read", "comment:delete",
            "version:create", "version:read"
        ));
        ROLE_PERMISSIONS.put("editor", Set.of(
            "doc:create", "doc:read", "doc:update", "doc:delete",
            "doc:share", "doc:export", "doc:import",
            "comment:create", "comment:read", "comment:delete",
            "version:create", "version:read"
        ));
        ROLE_PERMISSIONS.put("viewer", Set.of(
            "doc:read", "doc:export",
            "comment:read",
            "version:read"
        ));
    }

    public boolean hasPermission(String role, String permission) {
        Set<String> permissions = ROLE_PERMISSIONS.getOrDefault(role, Collections.emptySet());
        return permissions.contains(permission);
    }

    public Set<String> getPermissions(String role) {
        return ROLE_PERMISSIONS.getOrDefault(role, Collections.emptySet());
    }

    public List<String> getRoles() {
        return new ArrayList<>(ROLE_PERMISSIONS.keySet());
    }
}