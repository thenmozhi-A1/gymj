package com.example.gym.controller;

import com.example.gym.dto.UserRegistrationDTO;
import com.example.gym.entity.User;
import com.example.gym.service.UserService;
import com.example.gym.service.NotificationService;
import com.example.gym.service.AuditLogService;
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

    public UserController(UserService userService, NotificationService notificationService, AuditLogService auditLogService) {
        this.userService = userService;
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
