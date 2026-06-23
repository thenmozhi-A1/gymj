package com.example.gym.controller;
import org.springframework.transaction.annotation.Transactional; 
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
        staff.setExperience(staffDto.getExperience());
        staff.setLeaves(staffDto.getLeaves() != null ? staffDto.getLeaves() : 0);
        staff.setPermissions(staffDto.getPermissions() != null ? staffDto.getPermissions() : 0);
        
        staff.setDepartment(staffDto.getDepartment());
        staff.setJoiningDate(staffDto.getJoiningDate());
        staff.setEmergencyContactName(staffDto.getEmergencyContactName());
        staff.setEmergencyContactPhone(staffDto.getEmergencyContactPhone());
        staff.setBankName(staffDto.getBankName());
        staff.setAccountNumber(staffDto.getAccountNumber());
        staff.setIfscCode(staffDto.getIfscCode());
        staff.setDocuments(staffDto.getDocuments());
        staff.setProfilePhoto(staffDto.getProfilePhoto());
        
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
    public ResponseEntity<?> getStaffById(@PathVariable("id") Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(mapToDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** PUT /api/v1/staffs/{id} — Update staff details */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable("id") Long id, @Valid @RequestBody StaffDTO updatedStaffDto) {
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
                    staff.setExperience(updatedStaffDto.getExperience());
                    staff.setLeaves(updatedStaffDto.getLeaves() != null ? updatedStaffDto.getLeaves() : staff.getLeaves());
                    staff.setPermissions(updatedStaffDto.getPermissions() != null ? updatedStaffDto.getPermissions() : staff.getPermissions());
                    
                    if (updatedStaffDto.getDepartment() != null) staff.setDepartment(updatedStaffDto.getDepartment());
                    if (updatedStaffDto.getJoiningDate() != null) staff.setJoiningDate(updatedStaffDto.getJoiningDate());
                    if (updatedStaffDto.getEmergencyContactName() != null) staff.setEmergencyContactName(updatedStaffDto.getEmergencyContactName());
                    if (updatedStaffDto.getEmergencyContactPhone() != null) staff.setEmergencyContactPhone(updatedStaffDto.getEmergencyContactPhone());
                    if (updatedStaffDto.getBankName() != null) staff.setBankName(updatedStaffDto.getBankName());
                    if (updatedStaffDto.getAccountNumber() != null) staff.setAccountNumber(updatedStaffDto.getAccountNumber());
                    if (updatedStaffDto.getIfscCode() != null) staff.setIfscCode(updatedStaffDto.getIfscCode());
                    if (updatedStaffDto.getDocuments() != null) staff.setDocuments(updatedStaffDto.getDocuments());
                    if (updatedStaffDto.getProfilePhoto() != null) staff.setProfilePhoto(updatedStaffDto.getProfilePhoto());

                    User savedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok(mapToDTO(savedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/staffs/{id} — Delete staff */
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable("id") Long id) {
        try {
            if (userRepository.existsById(id)) {
                String name = userRepository.findById(id).map(User::getFullName).orElse("id=" + id);
                userRepository.deleteById(id);
                auditLogService.log("DELETE_STAFF", currentAdminEmail(), id, "Staff",
                        "Deleted staff: " + name);
                return ResponseEntity.ok(Map.of("message", "Staff deleted successfully"));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to delete staff: " + e.getMessage()));
        }
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

        if (user.getStaffDetails() != null) {
            Staff staff = user.getStaffDetails();
            dto.setStaffId(staff.getId());
            dto.setSalary(staff.getSalary());
            dto.setTimes(staff.getTimes());
            dto.setSpecialty(staff.getSpecialty());
            dto.setExperience(staff.getExperience());
            dto.setLeaves(staff.getLeaves());
            dto.setPermissions(staff.getPermissions());
            dto.setDepartment(staff.getDepartment());
            dto.setJoiningDate(staff.getJoiningDate());
            dto.setEmergencyContactName(staff.getEmergencyContactName());
            dto.setEmergencyContactPhone(staff.getEmergencyContactPhone());
            dto.setBankName(staff.getBankName());
            dto.setAccountNumber(staff.getAccountNumber());
            dto.setIfscCode(staff.getIfscCode());
            dto.setDocuments(staff.getDocuments());
            dto.setProfilePhoto(staff.getProfilePhoto());
        }
        return dto;
    }
}
