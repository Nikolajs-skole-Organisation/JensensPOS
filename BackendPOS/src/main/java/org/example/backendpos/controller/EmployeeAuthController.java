package org.example.backendpos.controller;

import org.example.backendpos.dto.LoginRequest;
import org.example.backendpos.dto.LoginResponse;
import org.example.backendpos.exception.InvalidPinException;
import org.example.backendpos.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service/auth")
public class EmployeeAuthController {
    private final EmployeeService employeeService;

    public EmployeeAuthController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return employeeService.loginWithPin(request.pinCode());
    }

    @ExceptionHandler(InvalidPinException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleInvalidPin(InvalidPinException ex) {
        return ex.getMessage();
    }
}
