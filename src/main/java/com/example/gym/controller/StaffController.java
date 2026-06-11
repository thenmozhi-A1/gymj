package com.example.gym.controller; 
import com.example.gym.dto.StaffDTO;
import com.example.gym.entity.Staff;
import com.example.gym.entity.User;
import com.example.gym.repository.UserRepository;
import com.example.gym.service.AuditLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staffs") // Fixed mapping to match frontend
public class StaffController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public StaffController(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    private String currentAdminEmail() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User u) return u.getEmail();
        } catch (Exception ignored) {}
        return "system";
    }

    /** POST /api/v1/staffs/register — Create a new staff record */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody StaffDTO staffDto) {
        if (userRepository.existsByEmail(staffDto.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered: " + staffDto.getEmail()));
        }

        User user = new User();
        user.setFullName(staffDto.getFullName());
        user.setEmail(staffDto.getEmail());
        user.setPassword(passwordEncoder.encode(staffDto.getPassword()));
        user.setPhone(staffDto.getPhone());
        user.setAddress(staffDto.getAddress());
        user.setRole(staffDto.getRole() != null ? staffDto.getRole() : "STAFF");
        user.setStatus(staffDto.getStatus() != null ? staffDto.getStatus() : "ACTIVE");

        Staff staff = new Staff();
        staff.setSalary(staffDto.getSalary());
        staff.setTimes(staffDto.getTimes());
        staff.setSpecialty(staffDto.getSpecialty());
        staff.setLeaves(staffDto.getLeaves() != null ? staffDto.getLeaves() : 0);
        staff.setPermissions(staffDto.getPermissions() != null ? staffDto.getPermissions() : 0);
        
        // Link bidirectional
        staff.setUser(user);
        user.setStaffDetails(staff);

        User savedUser = userRepository.save(user);
        auditLogService.log("ADD_STAFF", currentAdminEmail(), savedUser.getId(), "Staff",
                "Added staff: " + savedUser.getFullName() + " role=" + savedUser.getRole());
        return ResponseEntity.ok(mapToDTO(savedUser));
    }

    /** GET /api/v1/staffs — Get all staff members */
    @GetMapping
    public List<StaffDTO> getAllStaffs() {
        return userRepository.findByRoleIn(List.of("STAFF", "TRAINER", "FRONT OFFICE", "ADMIN"))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /** GET /api/v1/staffs/me — Get authenticated user's staff record */
    @GetMapping("/me")
    public ResponseEntity<?> getStaffMe() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        User virtualUser = (User) auth.getPrincipal();
        return userRepository.findById(virtualUser.getId())
                .map(user -> ResponseEntity.ok(mapToDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/v1/staffs/{id} — Get staff by ID */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStaffById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(mapToDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** PUT /api/v1/staffs/{id} — Update staff details */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @Valid @RequestBody StaffDTO updatedStaffDto) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setFullName(updatedStaffDto.getFullName());
                    existingUser.setPhone(updatedStaffDto.getPhone());
                    existingUser.setAddress(updatedStaffDto.getAddress());
                    existingUser.setRole(updatedStaffDto.getRole() != null ? updatedStaffDto.getRole() : existingUser.getRole());
                    existingUser.setStatus(updatedStaffDto.getStatus() != null ? updatedStaffDto.getStatus() : existingUser.getStatus());
                    
                    Staff staff = existingUser.getStaffDetails();
                    if (staff == null) {
                        staff = new Staff();
                        staff.setUser(existingUser);
                        existingUser.setStaffDetails(staff);
                    }
                    staff.setSalary(updatedStaffDto.getSalary());
                    staff.setTimes(updatedStaffDto.getTimes());
                    staff.setSpecialty(updatedStaffDto.getSpecialty());
                    staff.setLeaves(updatedStaffDto.getLeaves() != null ? updatedStaffDto.getLeaves() : staff.getLeaves());
                    staff.setPermissions(updatedStaffDto.getPermissions() != null ? updatedStaffDto.getPermissions() : staff.getPermissions());

                    User savedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok(mapToDTO(savedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/v1/staffs/{id} — Delete staff */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            String name = userRepository.findById(id).map(User::getFullName).orElse("id=" + id);
            userRepository.deleteById(id);
            auditLogService.log("DELETE_STAFF", currentAdminEmail(), id, "Staff",
                    "Deleted staff: " + name);
            return ResponseEntity.ok(Map.of("message", "Staff deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }

    private StaffDTO mapToDTO(User user) {
        StaffDTO dto = new StaffDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        // Do not return password hash
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setFingerprintHash(user.getFingerprintHash());
        dto.setFingerprintEnrolled(user.getFingerprintEnrolled());

        if (user.getStaffDetails() != null) {
            Staff staff = user.getStaffDetails();
            dto.setSalary(staff.getSalary());
            dto.setTimes(staff.getTimes());
            dto.setSpecialty(staff.getSpecialty());
            dto.setLeaves(staff.getLeaves());
            dto.setPermissions(staff.getPermissions());
        }
        return dto;
    }
}
