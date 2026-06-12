package com.example.gym.service;

import com.example.gym.dto.AttendanceResponse;
import com.example.gym.dto.AttendanceStatsResponse;
import java.util.List;

public interface AttendanceService {
    AttendanceResponse checkInUser(Long userId);
    AttendanceResponse checkInStaff(Long staffId);
    AttendanceResponse checkOut(Long attendanceId);
    List<AttendanceResponse> getByUser(Long userId);
    List<AttendanceResponse> getByStaff(Long staffId);
    List<AttendanceResponse> getAll();
    AttendanceStatsResponse getTodayStats();
}
