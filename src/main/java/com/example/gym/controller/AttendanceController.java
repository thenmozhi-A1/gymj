package com.example.gym.controller;

import com.example.gym.dto.AttendanceResponse;
import com.example.gym.dto.AttendanceStatsResponse;
import com.example.gym.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // Member check-in
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> checkInUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(attendanceService.checkInUser(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // Staff check-in
    @PostMapping("/staff/{staffId}")
    public ResponseEntity<?> checkInStaff(@PathVariable Long staffId) {
        try {
            return ResponseEntity.ok(attendanceService.checkInStaff(staffId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // Check-out (shared — works for both user and staff records)
    @PutMapping("/{id}/checkout")
    public ResponseEntity<?> checkOut(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(attendanceService.checkOut(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // Member history
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttendanceResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(attendanceService.getByUser(userId));
    }

    // Staff history
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<AttendanceResponse>> getByStaff(@PathVariable Long staffId) {
        return ResponseEntity.ok(attendanceService.getByStaff(staffId));
    }

    // Admin: all records
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getAll() {
        return ResponseEntity.ok(attendanceService.getAll());
    }

    // Admin: today's live stats
    @GetMapping("/stats/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceStatsResponse> getTodayStats() {
        return ResponseEntity.ok(attendanceService.getTodayStats());
    }
}
