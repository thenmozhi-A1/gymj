package com.example.gym.controller;

import com.example.gym.dto.UserRegistrationDTO;
import com.example.gym.entity.User;
import com.example.gym.service.UserService;
import com.example.gym.service.NotificationService;
import com.example.gym.service.AuditLogService;
import com.example.gym.service.ExpiryNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final ExpiryNotificationService expiryNotificationService;

    public UserController(UserService userService, NotificationService notificationService, AuditLogService auditLogService, ExpiryNotificationService expiryNotificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.expiryNotificationService = expiryNotificationService;
    }

    private String currentAdminEmail() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User u) return u.getEmail();
        } catch (Exception ignored) {}
        return "system";
    }

    /** POST /api/users/register — Create new account */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @jakarta.validation.Valid UserRegistrationDTO dto) {
        try {
            User user = new User();
            user.setFullName(dto.getFullName());
            user.setEmail(dto.getEmail());
            user.setPassword(dto.getPassword());
            user.setPhone(dto.getPhone());
            user.setAddress(dto.getAddress());
            user.setGender(dto.getGender());
            user.setMembershipPlan(dto.getMembershipPlan());
            user.setDob(dto.getDob());
            user.setAge(dto.getAge());
            user.setCity(dto.getCity());
            user.setHeight(dto.getHeight());
            user.setWeight(dto.getWeight());
            user.setBmi(dto.getBmi());
            user.setBloodGroup(dto.getBloodGroup());
            user.setFitnessGoal(dto.getFitnessGoal());
            user.setStartDate(dto.getStartDate());
            user.setExpiryDate(dto.getExpiryDate());
            user.setReferralSource(dto.getReferralSource());
            user.setEmergencyContactName(dto.getEmergencyContactName());
            user.setEmergencyContactNumber(dto.getEmergencyContactNumber());
            user.setMedicalConditions(dto.getMedicalConditions());
            user.setAllergies(dto.getAllergies());
            // Force role to USER for public registration
            user.setRole("USER");
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



    /** POST /api/users/{id}/send-reminder — Send expiry reminder via email + WhatsApp */
    @PostMapping("/{id}/send-reminder")
    public ResponseEntity<?> sendReminder(@PathVariable("id") Long id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            if ((user.getEmail() == null || user.getEmail().isBlank())
                    && (user.getPhone() == null || user.getPhone().isBlank())) {
                return ResponseEntity.badRequest().body(Map.of("error", "User has no email or phone number on file"));
            }

            Map<String, Object> result = expiryNotificationService.sendManualReminder(user);
            boolean emailSent = (boolean) result.get("emailSent");
            boolean whatsappSent = (boolean) result.get("whatsappSent");

            if (!emailSent && !whatsappSent) {
                // Both channels failed
                return ResponseEntity.status(500).body(result);
            }

            // At least one channel succeeded
            StringBuilder msg = new StringBuilder("Reminder sent via: ");
            if (emailSent) msg.append("📧 Email ");
            if (whatsappSent) msg.append("💬 WhatsApp ");
            result.put("message", msg.toString().trim());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send reminder: " + e.getMessage()));
        }
    }

    /** GET /api/users — Get all users (admin) */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /** GET /api/users/{id} — Get user by ID */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** PUT /api/users/{id} — Update user profile */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** DELETE /api/users/{id} — Delete user */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        try {
            User target = userService.getUserById(id);
            userService.deleteUser(id);
            auditLogService.log("DELETE_USER", currentAdminEmail(), id, "User",
                    "Deleted user: " + (target != null ? target.getFullName() + " (" + target.getEmail() + ")" : "id=" + id));
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete user: " + e.getMessage()));
        }
    }
}
