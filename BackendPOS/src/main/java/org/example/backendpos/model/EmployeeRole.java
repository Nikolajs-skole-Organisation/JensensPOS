package org.example.backendpos.model;

public enum EmployeeRole {
    STAFF,
    LEADER,
    CHIEF;

    public boolean hasAtLeast(EmployeeRole required) {
        return this.ordinal() >= required.ordinal();
    }
}
