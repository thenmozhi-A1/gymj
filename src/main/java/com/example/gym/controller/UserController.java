package com.example.gym.controller;

import com.example.gym.entity.User;
import com.example.gym.service.UserService;
import com.example.gym.service.NotificationService;
import com.example.gym.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.gym.security.JwtTokenProvider;
import com.example.gym.entity.RefreshToken;
import com.example.gym.repository.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public UserController(UserService userService, JwtTokenProvider tokenProvider, RefreshTokenRepository refreshTokenRepository, NotificationService notificationService, AuditLogService auditLogService) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    private String currentAdminEmail() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User u) return u.getEmail();
        } catch (Exception ignored) {}
        return "system";
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /** POST /api/users/register — Create new account */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User saved = userService.registerUser(user);
            notificationService.broadcast("NEW_MEMBER", Map.of(
                    "id", saved.getId(),
                    "name", saved.getFullName() != null ? saved.getFullName() : "",
                    "email", saved.getEmail() != null ? saved.getEmail() : "",
                    "role", saved.getRole() != null ? saved.getRole() : "USER"
            ));
            auditLogService.log("ADD_MEMBER", currentAdminEmail(), saved.getId(), "User",
                    "Registered new member: " + saved.getFullName() + " (" + saved.getEmail() + ")");
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/users/login — Login with email + password */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            User user = userService.loginUser(
                credentials.get("email"),
                credentials.get("password")
            );
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /api/users/credential — Get credential ID by email */
    @GetMapping("/credential")
    public ResponseEntity<?> getCredential(@RequestParam String email) {
        try {
            User user = userService.getUserByEmail(email);
            if (user != null && user.getFingerprintHash() != null) {
                return ResponseEntity.ok(Map.of("credentialId", user.getFingerprintHash()));
            }
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/users/biometric-login — Login with fingerprint hash lookup */
    @PostMapping("/biometric-login")
    public ResponseEntity<?> biometricLogin(@RequestBody Map<String, String> body) {
        try {
            User user = userService.loginBiometric(body.get("email"), body.get("fingerprintHash"));
            
            String accessToken = tokenProvider.generateAccessToken(user);
            String rawRefreshToken = tokenProvider.generateRefreshToken();
            
            RefreshToken rt = new RefreshToken(
                    user, 
                    hashToken(rawRefreshToken), 
                    LocalDateTime.now().plus(30, ChronoUnit.DAYS), 
                    "Biometric Device"
            );
            refreshTokenRepository.save(rt);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", rawRefreshToken);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "name", user.getFullName(),
                    "email", user.getEmail(),
                    "role", user.getRole()
            ));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }


    /** GET /api/users — Get all users (admin) */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /** GET /api/users/{id} — Get user by ID */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** PUT /api/users/{id} — Update user profile */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** DELETE /api/users/{id} — Delete user */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User target = userService.getUserById(id);
            userService.deleteUser(id);
            auditLogService.log("DELETE_USER", currentAdminEmail(), id, "User",
                    "Deleted user: " + (target != null ? target.getFullName() + " (" + target.getEmail() + ")" : "id=" + id));
        } catch (Exception e) {
            userService.deleteUser(id);
        }
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
