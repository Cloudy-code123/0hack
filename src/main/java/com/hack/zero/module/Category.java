package com.hack.zero.module;

public enum Category {
    MOVEMENT("Movement"),
    COMBAT("Combat"),
    PLAYER("Player"),
    RENDER("Render");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
