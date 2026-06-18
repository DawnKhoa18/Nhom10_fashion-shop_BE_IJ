package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.Customer;
import com.example.demo.model.Employee;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository repository;
    private final EmployeeRepository employeeRepository;
    private final JavaMailSender mailSender;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    private final Map<String, OtpData> passwordResetOtps = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

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
        customer.setCreatedAt(LocalDateTime.now());
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
            return ResponseEntity.ok(customerLoginResponse(customer, "Đăng nhập thành công"));
        }

        if (employeeOpt.isPresent()
                && employeeOpt.get().getPassword().equals(request.getPassword())) {
            Employee employee = employeeOpt.get();
            employee.setLastLogin(LocalDateTime.now());
            employeeRepository.save(employee);
            return ResponseEntity.ok(employeeLoginResponse(employee, "Đăng nhập thành công"));
        }

        if (customerOpt.isPresent() || employeeOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai mật khẩu");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email không tồn tại");
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> request) {
        String idToken = request.get("credential");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body("Thiếu Google token");
        }

        if (googleClientId == null || googleClientId.isBlank() || googleClientId.contains("YOUR_GOOGLE_CLIENT_ID")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Chưa cấu hình google.client-id trong application.properties");
        }

        Map<String, Object> googleUser;
        try {
            googleUser = verifyGoogleToken(idToken);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token Google không hợp lệ");
        }

        String audience = String.valueOf(googleUser.get("aud"));
        if (!googleClientId.equals(audience)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google Client ID không khớp");
        }

        String emailVerified = String.valueOf(googleUser.get("email_verified"));
        if (!"true".equalsIgnoreCase(emailVerified)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email Google chưa được xác thực");
        }

        String email = normalizeEmail(String.valueOf(googleUser.get("email")));
        String fullName = String.valueOf(googleUser.getOrDefault("name", email));

        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setLastLogin(LocalDateTime.now());
            employeeRepository.save(employee);
            return ResponseEntity.ok(employeeLoginResponse(employee, "Đăng nhập Google thành công"));
        }

        Customer customer = repository.findByEmail(email).orElseGet(() -> {
            Customer newCustomer = new Customer();
            newCustomer.setFullName(fullName);
            newCustomer.setEmail(email);
            newCustomer.setPhone("0000000000");
            newCustomer.setPassword("GOOGLE_LOGIN_" + UUID.randomUUID());
            newCustomer.setGender("Khác");
            newCustomer.setCreatedAt(LocalDateTime.now());
            newCustomer.setStatus(1);
            return newCustomer;
        });

        customer.setLastLogin(LocalDateTime.now());
        repository.save(customer);

        return ResponseEntity.ok(customerLoginResponse(customer, "Đăng nhập Google thành công"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = normalizeEmail(request.get("email"));
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập email");
        }

        Optional<Customer> customerOpt = repository.findByEmail(email);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email không tồn tại trong hệ thống khách hàng");
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        passwordResetOtps.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(5)));

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (mailUsername != null && !mailUsername.isBlank()) {
                message.setFrom(mailUsername);
            }
            message.setTo(email);
            message.setSubject("Mã OTP đặt lại mật khẩu Fashion 4Men");
            message.setText("Mã OTP của bạn là: " + otp + "\nMã có hiệu lực trong 5 phút.");
            mailSender.send(message);
        } catch (Exception e) {
            passwordResetOtps.remove(email);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không gửi được email OTP. Kiểm tra cấu hình Gmail App Password.");
        }

        return ResponseEntity.ok("Đã gửi mã OTP về email của bạn");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = normalizeEmail(request.get("email"));
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        if (email == null || email.isBlank()) return ResponseEntity.badRequest().body("Vui lòng nhập email");
        if (otp == null || otp.isBlank()) return ResponseEntity.badRequest().body("Vui lòng nhập mã OTP");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("Xác nhận mật khẩu không khớp");
        }

        OtpData otpData = passwordResetOtps.get(email);
        if (otpData == null || otpData.expiresAt().isBefore(LocalDateTime.now())) {
            passwordResetOtps.remove(email);
            return ResponseEntity.badRequest().body("Mã OTP đã hết hạn hoặc không tồn tại");
        }
        if (!otpData.code().equals(otp)) {
            return ResponseEntity.badRequest().body("Mã OTP không đúng");
        }

        Optional<Customer> customerOpt = repository.findByEmail(email);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email không tồn tại");
        }

        Customer customer = customerOpt.get();
        customer.setPassword(newPassword);
        repository.save(customer);
        passwordResetOtps.remove(email);

        return ResponseEntity.ok("Đổi mật khẩu thành công");
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

        employee.setLastLogin(LocalDateTime.now());
        employeeRepository.save(employee);
        return ResponseEntity.ok(employeeLoginResponse(employee, "Đăng nhập thành công"));
    }

    private Map<String, Object> customerLoginResponse(Customer customer, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("accountType", "CUSTOMER");
        response.put("role", "CUSTOMER");
        response.put("customerId", customer.getId());
        response.put("fullName", customer.getFullName());
        response.put("email", customer.getEmail());
        return response;
    }

    private Map<String, Object> employeeLoginResponse(Employee employee, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("accountType", "EMPLOYEE");
        response.put("role", employee.getRole());
        response.put("employeeId", employee.getId());
        response.put("fullName", employee.getFullName());
        response.put("email", employee.getEmail());
        return response;
    }

    private Map<String, Object> verifyGoogleToken(String idToken) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://oauth2.googleapis.com/tokeninfo")
                .queryParam("id_token", idToken)
                .toUriString();

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, Map.class);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private record OtpData(String code, LocalDateTime expiresAt) {
    }
}
