package org.example.backendpos.repository;

import org.example.backendpos.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByPinCode(String pinCode);
}
