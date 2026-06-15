package com.example.gym.service;

import com.example.gym.dto.AttendanceResponse;
import com.example.gym.dto.AttendanceStatsResponse;
import com.example.gym.entity.Attendance;
import com.example.gym.entity.AttendanceMethod;
import com.example.gym.entity.Staff;
import com.example.gym.entity.User;
import com.example.gym.repository.AttendanceRepository;
import com.example.gym.repository.StaffRepository;
import com.example.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixConstraints() {
        try {
            jdbcTemplate.execute("ALTER TABLE attendance DROP FOREIGN KEY FKrogowg5617tejib9qe94rvgyi");
        } catch (Exception e) {
            // ignore if it doesn't exist
        }
    }
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    @Transactional
    public AttendanceResponse checkInUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (attendanceRepository.existsByUserIdAndAttendanceDateAndCheckOutTimeIsNull(userId, LocalDate.now())) {
            throw new RuntimeException("Already checked in today. Please check out first.");
        }

        Attendance attendance = Attendance.builder()
                .user(user)
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.now())
                .method(AttendanceMethod.MANUAL)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        notificationService.broadcast("ATTENDANCE", java.util.Map.of("name", user.getFullName() != null ? user.getFullName() : user.getEmail()));
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse checkInStaff(Long staffId) {
        // Find staff by User ID because frontend passes user.id
        Staff staff = staffRepository.findByUserId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found for user id: " + staffId));

        if (attendanceRepository.existsByStaffIdAndAttendanceDateAndCheckOutTimeIsNull(staff.getId(), LocalDate.now())) {
            throw new RuntimeException("Already checked in today. Please check out first.");
        }

        Attendance attendance = Attendance.builder()
                .staff(staff)
                .attendanceDate(LocalDate.now())
                .checkInTime(LocalTime.now())
                .method(AttendanceMethod.MANUAL)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        notificationService.broadcast("ATTENDANCE", java.util.Map.of("name", staff.getUser() != null && staff.getUser().getFullName() != null ? staff.getUser().getFullName() : "Staff Member"));
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found with id: " + attendanceId));

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Already checked out.");
        }

        attendance.setCheckOutTime(LocalTime.now());
        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    @Override
    public List<AttendanceResponse> getByUser(Long userId) {
        return attendanceRepository.findByUserIdOrderByAttendanceDateDescCheckInTimeDesc(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponse> getByStaff(Long staffId) {
        Staff staff = staffRepository.findByUserId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found for user id: " + staffId));
        
        return attendanceRepository.findByStaffIdOrderByAttendanceDateDescCheckInTimeDesc(staff.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponse> getAll() {
        return attendanceRepository.findAllByOrderByAttendanceDateDescCheckInTimeDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public AttendanceStatsResponse getTodayStats() {
        LocalDate today = LocalDate.now();
        long totalCheckIns = attendanceRepository.countByDate(today);
        long activeInside = attendanceRepository.countActiveInside(today);
        
        Optional<Object[]> peakHourData = attendanceRepository.findPeakHour(today);
        String peakHourStr = "—";
        if (peakHourData.isPresent() && peakHourData.get() != null) {
            Object[] data = peakHourData.get();
            if (data.length > 0 && data[0] != null) {
                int hour = ((Number) data[0]).intValue();
                peakHourStr = String.format("%02d:00", hour);
            }
        }

        return AttendanceStatsResponse.builder()
                .today(today.toString())
                .totalCheckIns(totalCheckIns)
                .activeInside(activeInside)
                .peakHour(peakHourStr)
                .build();
    }

    private AttendanceResponse mapToResponse(Attendance record) {
        String attDateStr = record.getAttendanceDate().toString();
        String inTimeStr = record.getCheckInTime().format(TIME_FORMATTER);
        String outTimeStr = record.getCheckOutTime() != null ? record.getCheckOutTime().format(TIME_FORMATTER) : null;

        AttendanceResponse.AttendanceResponseBuilder builder = AttendanceResponse.builder()
                .id(record.getId())
                .attendanceDate(attDateStr)
                .date(attDateStr)
                .checkInTime(inTimeStr)
                .entry(inTimeStr)
                .checkOutTime(outTimeStr)
                .exit(outTimeStr)
                .method(record.getMethod().name());

        if (record.getUser() != null) {
            builder.user(AttendanceResponse.UserSummary.builder()
                    .id(record.getUser().getId())
                    .fullName(record.getUser().getFullName())
                    .memberId(record.getUser().getId().toString())
                    .role("MEMBER")
                    .build());
            builder.role("MEMBER");
            builder.fullName(record.getUser().getFullName());
        } else if (record.getStaff() != null && record.getStaff().getUser() != null) {
            String roleName = record.getStaff().getUser().getRole() != null ? record.getStaff().getUser().getRole() : "STAFF";
            builder.staff(AttendanceResponse.StaffSummary.builder()
                    .id(record.getStaff().getId())
                    .fullName(record.getStaff().getUser().getFullName())
                    .role(roleName)
                    .build());
            builder.role(roleName);
            builder.fullName(record.getStaff().getUser().getFullName());
        }

        return builder.build();
    }
}
