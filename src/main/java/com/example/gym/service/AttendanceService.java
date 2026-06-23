package com.example.gym.service;

import com.example.gym.entity.Attendance;
import com.example.gym.entity.Staff;
import com.example.gym.entity.User;
import com.example.gym.repository.AttendanceRepository;
import com.example.gym.repository.StaffRepository;
import com.example.gym.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, UserRepository userRepository, StaffRepository staffRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
    }

    /** Mark attendance (check-in) for a user */
    public Attendance markAttendance(Long userId, Attendance attendance) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        attendance.setUser(user);
        checkIfLate(attendance);
        return attendanceRepository.save(attendance);
    }

    public Attendance markStaffAttendance(Long userId, Attendance attendance) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Staff staff = user.getStaffDetails();
        if (staff == null) {
            throw new RuntimeException("Staff not found with id: " + userId);
        }
        attendance.setStaff(staff);
        attendance.setUser(user);
        checkIfLate(attendance);
        return attendanceRepository.save(attendance);
    }

    /** Get all attendance records for a specific user */
    public List<Attendance> getAttendanceByUser(Long userId) {
        return attendanceRepository.findByUserId(userId);
    }

    /** Get all attendance records for a specific staff using userId */
    public List<Attendance> getAttendanceByStaff(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getStaffDetails() != null) {
            return attendanceRepository.findByStaffId(user.getStaffDetails().getId());
        }
        return List.of();
    }

    /** Get attendance for a specific user on a specific date */
    public List<Attendance> getAttendanceByUserAndDate(Long userId, LocalDate date) {
        return attendanceRepository.findByUserIdAndAttendanceDate(userId, date);
    }

    /** Get all attendance for a given date (admin view) */
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date);
    }

    /** Get all attendance records (admin) */
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    /** Update check-out time */
    public Attendance updateCheckOut(Long id, Attendance updated) {
        Attendance existing = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found: " + id));
        
        if (updated != null && updated.getCheckOutTime() != null) {
            existing.setCheckOutTime(updated.getCheckOutTime());
        } else {
            existing.setCheckOutTime(java.time.LocalTime.now());
        }
        
        if (updated != null && updated.getNotes() != null) {
            existing.setNotes(updated.getNotes());
        }
        
        return attendanceRepository.save(existing);
    }

    /** Delete attendance record */
    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }

    public Attendance requestCorrection(Long id, String reason) {
        Attendance existing = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        existing.setCorrectionReason(reason);
        existing.setCorrectionStatus("PENDING");
        return attendanceRepository.save(existing);
    }

    public Attendance updateCorrectionStatus(Long id, String status) {
        Attendance existing = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        existing.setCorrectionStatus(status);
        if ("APPROVED".equals(status)) {
            // Here you might normally apply the requested correction times
            // For now, we just approve it
        }
        return attendanceRepository.save(existing);
    }

    private void checkIfLate(Attendance attendance) {
        if (attendance.getCheckInTime() != null) {
            // Assume 09:15 AM is the cutoff for late
            java.time.LocalTime cutoff = java.time.LocalTime.of(9, 15);
            if (attendance.getCheckInTime().isAfter(cutoff)) {
                attendance.setIsLate(true);
            } else {
                attendance.setIsLate(false);
            }
        }
    }
}
