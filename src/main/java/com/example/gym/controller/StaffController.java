package com.example.gym.controller;

import com.example.gym.entity.Staff;
import com.example.gym.repository.StaffRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staffs")
public class StaffController {

    private final StaffRepository staffRepository;

    public StaffController(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    /** POST /api/staffs/register — Create a new staff record */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Staff staff) {
        if (staffRepository.existsByEmail(staff.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered: " + staff.getEmail()));
        }
        Staff saved = staffRepository.save(staff);
        return ResponseEntity.ok(saved);
    }

    /** GET /api/staffs — Get all staff members */
    @GetMapping
    public List<Staff> getAllStaffs() {
        return staffRepository.findAll();
    }

    /** GET /api/staffs/{id} — Get staff by ID */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStaffById(@PathVariable Long id) {
        return staffRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** PUT /api/staffs/{id} — Update staff details */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @RequestBody Staff updatedStaff) {
        return staffRepository.findById(id)
                .map(existing -> {
                    existing.setFullName(updatedStaff.getFullName());
                    existing.setPhone(updatedStaff.getPhone());
                    existing.setAddress(updatedStaff.getAddress());
                    existing.setRole(updatedStaff.getRole());
                    existing.setSalary(updatedStaff.getSalary());
                    existing.setTimes(updatedStaff.getTimes());
                    existing.setSpecialty(updatedStaff.getSpecialty());
                    existing.setLeaves(updatedStaff.getLeaves());
                    existing.setPermissions(updatedStaff.getPermissions());
                    existing.setFingerprintHash(updatedStaff.getFingerprintHash());
                    existing.setFingerprintEnrolled(updatedStaff.getFingerprintEnrolled());
                    existing.setStatus(updatedStaff.getStatus());
                    Staff saved = staffRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/staffs/{id} — Delete staff */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        if (staffRepository.existsById(id)) {
            staffRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Staff deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
