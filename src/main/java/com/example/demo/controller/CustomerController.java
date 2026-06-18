package com.example.demo.controller;

import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository repository;

    @GetMapping
    public List<Customer> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        Customer customer = repository.findById(id).orElse(null);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toProfile(customer));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Customer customer = repository.findById(id).orElse(null);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        String fullName = value(payload, "fullName");
        String phone = value(payload, "phone");
        String gender = value(payload, "gender");
        String address = value(payload, "address");
        String hobby = value(payload, "hobby");

        if (fullName.length() < 2 || !fullName.matches("^[\\p{L}]+(?:[\\s'.-][\\p{L}]+)*$")) {
            return badRequest("Họ tên không hợp lệ");
        }
        if (!phone.matches("^0[0-9]{9,10}$")) {
            return badRequest("Số điện thoại phải bắt đầu bằng 0 và có 10-11 số");
        }
        if (gender.isBlank()) {
            return badRequest("Vui lòng chọn giới tính");
        }

        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setGender(gender);
        customer.setAddress(address);
        customer.setHobby(hobby);
        repository.save(customer);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật thông tin thành công");
        response.put("profile", toProfile(customer));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Customer customer = repository.findById(id).orElse(null);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        String currentPassword = value(payload, "currentPassword");
        String newPassword = value(payload, "newPassword");
        String confirmPassword = value(payload, "confirmPassword");

        if (!customer.getPassword().equals(currentPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Mật khẩu hiện tại không đúng"));
        }
        if (newPassword.length() < 6) {
            return badRequest("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        if (!newPassword.equals(confirmPassword)) {
            return badRequest("Xác nhận mật khẩu không khớp");
        }
        if (newPassword.equals(currentPassword)) {
            return badRequest("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        customer.setPassword(newPassword);
        repository.save(customer);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đổi mật khẩu thành công"
        ));
    }

    private Map<String, Object> toProfile(Customer customer) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", customer.getId());
        profile.put("fullName", customer.getFullName());
        profile.put("email", customer.getEmail());
        profile.put("phone", customer.getPhone());
        profile.put("gender", customer.getGender());
        profile.put("address", customer.getAddress());
        profile.put("hobby", customer.getHobby());
        profile.put("createdAt", customer.getCreatedAt());
        return profile;
    }

    private String value(Map<String, Object> payload, String key) {
        Object rawValue = payload.get(key);
        return rawValue == null ? "" : rawValue.toString().trim();
    }

    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", message
        ));
    }
}
