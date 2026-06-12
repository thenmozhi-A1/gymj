package com.example.gym.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceStatsResponse {
    private String today;          // LocalDate.now().toString()
    private long totalCheckIns;
    private long activeInside;
    private String peakHour;       // e.g. "06:00 AM" derived from DB aggregate
}
