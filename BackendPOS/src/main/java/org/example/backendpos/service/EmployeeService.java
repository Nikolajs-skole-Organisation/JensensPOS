package org.example.backendpos.service;

import org.example.backendpos.dto.LoginResponse;
import org.example.backendpos.exception.InvalidPinException;
import org.example.backendpos.model.Employee;
import org.example.backendpos.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public LoginResponse loginWithPin(String pinCode) {
        if (pinCode == null || !pinCode.matches("\\d{4}")) {
            throw new InvalidPinException("PIN skal være præcis 4 cifre.");
        }

        Employee emp = employeeRepository.findByPinCode(pinCode)
                .orElseThrow(() -> new InvalidPinException("Forkert PIN-kode."));

        return new LoginResponse(emp.getId(), emp.getName(), emp.getRole());
    }
}
