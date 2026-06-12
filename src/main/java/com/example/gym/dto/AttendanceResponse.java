package com.example.gym.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceResponse {
    private Long id;
    private String attendanceDate;   // "YYYY-MM-DD" via LocalDate.now().toString()
    private String checkInTime;      // "HH:mm:ss"
    private String checkOutTime;     // null or "HH:mm:ss"
    private String method;
    private String date;             // same as attendanceDate
    private String entry;            // same as checkInTime
    private String exit;             // same as checkOutTime
    private UserSummary user;        // non-null for member records
    private StaffSummary staff;      // non-null for staff records
    private String role;             // top-level role for AttendanceModule filter
    private String fullName;         // top-level name fallback

    @Data
    @Builder
    public static class UserSummary {
        private Long id;
        private String fullName;
        private String memberId;
        private String role;         // always "MEMBER"
    }

    @Data
    @Builder
    public static class StaffSummary {
        private Long id;
        private String fullName;
        private String role;         // TRAINER / STAFF / FRONT_OFFICE
    }
}
