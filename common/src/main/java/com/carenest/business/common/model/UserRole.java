package com.carenest.business.common.model;

public enum UserRole {
    ADMIN("ADMIN"),
    GUARDIAN("GUARDIAN"),
    CAREGIVER("CAREGIVER"),
    SYSTEM("SYSTEM");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}