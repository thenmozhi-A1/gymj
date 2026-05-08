package com.example.gym.controller;

import com.example.gym.entity.Attendance;
import com.example.gym.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /** POST /api/attendance/user/{userId} — Mark attendance (check-in) */
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> markAttendance(@PathVariable Long userId, @RequestBody Attendance attendance) {
        try {
            return ResponseEntity.ok(attendanceService.markAttendance(userId, attendance));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /api/attendance — Get all attendance (admin) */
    @GetMapping
    public List<Attendance> getAllAttendance() {
        return attendanceService.getAllAttendance();
    }

    /** GET /api/attendance/user/{userId} — Get all attendance for a user */
    @GetMapping("/user/{userId}")
    public List<Attendance> getAttendanceByUser(@PathVariable Long userId) {
        return attendanceService.getAttendanceByUser(userId);
    }

    /** GET /api/attendance/date?date=2024-05-01 — Get all attendance for a date */
    @GetMapping("/date")
    public List<Attendance> getAttendanceByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.getAttendanceByDate(date);
    }

    /** GET /api/attendance/user/{userId}/date?date=2024-05-01 — Get user attendance for a specific date */
    @GetMapping("/user/{userId}/date")
    public List<Attendance> getAttendanceByUserAndDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.getAttendanceByUserAndDate(userId, date);
    }

    /** PUT /api/attendance/{id}/checkout — Update check-out time */
    @PutMapping("/{id}/checkout")
    public ResponseEntity<?> updateCheckOut(@PathVariable Long id, @RequestBody Attendance attendance) {
        try {
            return ResponseEntity.ok(attendanceService.updateCheckOut(id, attendance));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** DELETE /api/attendance/{id} — Delete attendance record */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(Map.of("message", "Attendance record deleted"));
    }
}
