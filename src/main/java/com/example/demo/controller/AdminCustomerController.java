package com.example.demo.controller;

import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final CustomerRepository customerRepository;

    @GetMapping
    public List<Map<String, Object>> getCustomers() {
        return customerRepository.findAll().stream()
                .sorted(Comparator.comparing(Customer::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .<ResponseEntity<?>>map(customer -> ResponseEntity.ok(toResponse(customer)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody Map<String, Object> payload) {
        String error = validate(payload, null, true);
        if (error != null) return badRequest(error);

        Customer customer = new Customer();
        applyPayload(customer, payload, true);
        customer.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(toResponse(customerRepository.save(customer)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) return ResponseEntity.notFound().build();

        String error = validate(payload, id, false);
        if (error != null) return badRequest(error);

        applyPayload(customer, payload, false);
        return ResponseEntity.ok(toResponse(customerRepository.save(customer)));
    }

    private String validate(Map<String, Object> payload, Long currentId, boolean requirePassword) {
        String fullName = value(payload, "fullName");
        String email = value(payload, "email").toLowerCase();
        String phone = value(payload, "phone");
        String password = value(payload, "password");

        if (fullName.length() < 2) return "Họ tên phải có ít nhất 2 ký tự";
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return "Email không đúng định dạng";
        if (!phone.matches("^0[0-9]{9,10}$")) return "Số điện thoại phải bắt đầu bằng 0 và có 10-11 số";
        if (requirePassword && password.length() < 6) return "Mật khẩu phải có ít nhất 6 ký tự";
        if (!password.isBlank() && password.length() < 6) return "Mật khẩu phải có ít nhất 6 ký tự";

        Customer sameEmail = customerRepository.findByEmail(email).orElse(null);
        if (sameEmail != null && !sameEmail.getId().equals(currentId)) return "Email đã tồn tại";
        return null;
    }

    private void applyPayload(Customer customer, Map<String, Object> payload, boolean creating) {
        customer.setFullName(value(payload, "fullName"));
        customer.setEmail(value(payload, "email").toLowerCase());
        customer.setPhone(value(payload, "phone"));
        customer.setGender(defaultValue(value(payload, "gender"), "Khác"));
        customer.setAddress(value(payload, "address"));
        customer.setHobby(value(payload, "hobby"));
        customer.setStatus(intValue(payload.get("status"), 1));

        String password = value(payload, "password");
        if (creating || !password.isBlank()) customer.setPassword(password);
    }

    private Map<String, Object> toResponse(Customer customer) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("maKH", customer.getId());
        response.put("hoTenKH", customer.getFullName());
        response.put("email", customer.getEmail());
        response.put("sdt", customer.getPhone());
        response.put("gioiTinh", customer.getGender());
        response.put("diaChi", customer.getAddress());
        response.put("soThich", customer.getHobby());
        response.put("trangThai", customer.getStatus());
        response.put("ngayTao", customer.getCreatedAt());
        response.put("lanDangNhapGanNhat", customer.getLastLogin());
        return response;
    }

    private String value(Map<String, Object> payload, String key) {
        Object raw = payload.get(key);
        return raw == null ? "" : raw.toString().trim();
    }

    private String defaultValue(String value, String fallback) {
        return value.isBlank() ? fallback : value;
    }

    private int intValue(Object raw, int fallback) {
        try {
            return raw == null ? fallback : Integer.parseInt(raw.toString());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}
