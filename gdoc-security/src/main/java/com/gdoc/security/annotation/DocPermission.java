package com.gdoc.security.annotation;

public enum DocPermission {
    OWNER,
    EDITOR,
    VIEWER;

    public boolean covers(DocPermission required) {
        return this.ordinal() <= required.ordinal();
    }
}
