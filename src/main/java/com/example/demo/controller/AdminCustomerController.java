package com.example.demo.controller;

import com.example.demo.dto.AdminCustomerRequest;
import com.example.demo.dto.AdminCustomerResponse;
import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    public List<AdminCustomerResponse> getCustomers() {
        return customerRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Customer::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminCustomerResponse> getCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(customer -> ResponseEntity.ok(toResponse(customer)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createCustomer(@RequestBody AdminCustomerRequest request) {
        ResponseEntity<?> validation = validate(request, null, true);
        if (validation != null) {
            return validation;
        }

        Customer customer = new Customer();
        applyRequest(customer, request, true);
        customer.setCreatedAt(LocalDateTime.now());
        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(toResponse(savedCustomer));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody AdminCustomerRequest request) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        ResponseEntity<?> validation = validate(request, id, false);
        if (validation != null) {
            return validation;
        }

        applyRequest(customer, request, false);
        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(toResponse(savedCustomer));
    }

    private void applyRequest(Customer customer, AdminCustomerRequest request, boolean createMode) {
        customer.setFullName(clean(request.getFullName()));
        customer.setEmail(clean(request.getEmail()).toLowerCase());
        customer.setPhone(clean(request.getPhone()));
        customer.setGender(clean(request.getGender()).isBlank() ? "Khác" : clean(request.getGender()));
        customer.setAddress(clean(request.getAddress()));
        customer.setHobby(clean(request.getHobby()));
        customer.setStatus(request.getStatus() == null ? 1 : request.getStatus());

        String password = clean(request.getPassword());
        if (createMode || !password.isBlank()) {
            customer.setPassword(password);
        }
    }

    private ResponseEntity<?> validate(AdminCustomerRequest request, Long currentId, boolean createMode) {
        String fullName = clean(request.getFullName());
        String email = clean(request.getEmail()).toLowerCase();
        String phone = clean(request.getPhone());
        String password = clean(request.getPassword());
        String gender = clean(request.getGender());
        Integer status = request.getStatus();

        if (fullName.length() < 2) {
            return badRequest("Ho ten phai co it nhat 2 ky tu.");
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return badRequest("Email khong hop le.");
        }
        if (currentId == null && customerRepository.existsByEmail(email)) {
            return badRequest("Email da ton tai.");
        }
        if (currentId != null && customerRepository.existsByEmailAndIdNot(email, currentId)) {
            return badRequest("Email da ton tai.");
        }
        if (!phone.matches("^0[0-9]{9,10}$")) {
            return badRequest("So dien thoai phai bat dau bang 0 va co 10-11 so.");
        }
        if (createMode && password.length() < 6) {
            return badRequest("Mat khau phai co it nhat 6 ky tu.");
        }
        if (!password.isBlank() && password.length() < 6) {
            return badRequest("Mat khau phai co it nhat 6 ky tu.");
        }
        if (!gender.isBlank() && !List.of("Nam", "Nữ", "Khác").contains(gender)) {
            return badRequest("Gioi tinh khong hop le.");
        }
        if (status != null && status != 0 && status != 1) {
            return badRequest("Trang thai khong hop le.");
        }
        return null;
    }

    private AdminCustomerResponse toResponse(Customer customer) {
        return new AdminCustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getGender(),
                customer.getAddress(),
                customer.getHobby(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getLastLogin()
        );
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}
