package com.example.gym.service;

import com.example.gym.entity.Attendance;
import com.example.gym.entity.User;
import com.example.gym.repository.AttendanceRepository;
import com.example.gym.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    /** Mark attendance (check-in) for a user */
    public Attendance markAttendance(Long userId, Attendance attendance) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        attendance.setUser(user);
        return attendanceRepository.save(attendance);
    }

    /** Get all attendance records for a specific user */
    public List<Attendance> getAttendanceByUser(Long userId) {
        return attendanceRepository.findByUserId(userId);
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
        existing.setCheckOutTime(updated.getCheckOutTime());
        existing.setNotes(updated.getNotes());
        return attendanceRepository.save(existing);
    }

    /** Delete attendance record */
    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }
}
