package com.example.demo.controller;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.model.Customer;
import com.example.demo.model.Employee;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository repository;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors()
                    .get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMsg);
        }

        if (repository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại");
        }

        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setPassword(request.getPassword());
        customer.setGender("Khác");
        customer.setStatus(1);
        repository.save(customer);

        return ResponseEntity.ok("Đăng ký thành công");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors()
                    .get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMsg);
        }

        Optional<Customer> customerOpt = repository.findByEmail(request.getEmail());
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(request.getEmail());

        if (customerOpt.isPresent()
                && customerOpt.get().getPassword().equals(request.getPassword())) {
            Customer customer = customerOpt.get();
            customer.setLastLogin(LocalDateTime.now());
            repository.save(customer);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng nhập thành công");
            response.put("accountType", "CUSTOMER");
            response.put("role", "CUSTOMER");
            response.put("customerId", customer.getId());
            response.put("fullName", customer.getFullName());
            response.put("email", customer.getEmail());
            return ResponseEntity.ok(response);
        }

        if (employeeOpt.isPresent()
                && employeeOpt.get().getPassword().equals(request.getPassword())) {
            Employee employee = employeeOpt.get();
            employee.setLastLogin(LocalDateTime.now());
            employeeRepository.save(employee);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng nhập thành công");
            response.put("accountType", "EMPLOYEE");
            response.put("role", employee.getRole());
            response.put("employeeId", employee.getId());
            response.put("fullName", employee.getFullName());
            response.put("email", employee.getEmail());
            return ResponseEntity.ok(response);
        }

        if (customerOpt.isPresent() || employeeOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai mật khẩu");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email không tồn tại");
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(
            @Valid @RequestBody LoginRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors()
                    .get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMsg);
        }

        Optional<Employee> employeeOpt = employeeRepository.findByEmail(request.getEmail());

        if (employeeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email không tồn tại");
        }

        Employee employee = employeeOpt.get();

        if (!employee.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai mật khẩu");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng nhập thành công");
        response.put("employeeId", employee.getId());
        response.put("email", employee.getEmail());
        response.put("role", employee.getRole());

        return ResponseEntity.ok(response);
    }
}
