package org.example.backendpos.dto;

import org.example.backendpos.model.EmployeeRole;

public record LoginResponse(
        Long employeeId,
        String name,
        EmployeeRole role) {
}
