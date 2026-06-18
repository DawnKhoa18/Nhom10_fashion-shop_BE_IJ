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

        // Trả về lỗi validation đầu tiên nếu có
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
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors()
                    .get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMsg);
        }

        Optional<Customer> customerOpt = repository.findByEmail(request.getEmail());
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email không tồn tại");
        }

        Customer customer = customerOpt.get();
        if (!customer.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai mật khẩu");
        }

        return ResponseEntity.ok("Đăng nhập thành công");
    }

    @PostMapping("/admin/login")
    public ResponseEntity<String> adminLogin(
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

        return ResponseEntity.ok("Đăng nhập thành công - " + employee.getRole());
    }
}