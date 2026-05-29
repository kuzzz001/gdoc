package com.gdoc.security.annotation;

import java.util.Set;

public enum DocPermission {
    VIEWER(Set.of(PermissionAction.READ)),
    EDITOR(Set.of(PermissionAction.READ, PermissionAction.WRITE)),
    OWNER(Set.of(PermissionAction.READ, PermissionAction.WRITE, PermissionAction.DELETE, PermissionAction.SHARE, PermissionAction.MANAGE));

    private final Set<PermissionAction> actions;

    DocPermission(Set<PermissionAction> actions) {
        this.actions = actions;
    }

    public Set<PermissionAction> getActions() {
        return actions;
    }

    public boolean allows(PermissionAction action) {
        return actions.contains(action);
    }

    public boolean covers(DocPermission required) {
        return this.ordinal() >= required.ordinal();
    }

    public enum PermissionAction {
        READ,
        WRITE,
        DELETE,
        SHARE,
        MANAGE
    }
}