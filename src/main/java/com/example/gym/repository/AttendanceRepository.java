package com.example.gym.repository;

import com.example.gym.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Member history — newest first
    List<Attendance> findByUserIdOrderByAttendanceDateDescCheckInTimeDesc(Long userId);

    // Staff history — newest first
    List<Attendance> findByStaffIdOrderByAttendanceDateDescCheckInTimeDesc(Long staffId);

    // Admin: all records — newest first
    List<Attendance> findAllByOrderByAttendanceDateDescCheckInTimeDesc();

    // Prevent duplicate open check-ins on the same day
    boolean existsByUserIdAndAttendanceDateAndCheckOutTimeIsNull(Long userId, LocalDate date);
    boolean existsByStaffIdAndAttendanceDateAndCheckOutTimeIsNull(Long staffId, LocalDate date);

    // Today's stats for admin overview
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :today")
    long countByDate(@Param("today") LocalDate today);

    // Active inside count (checked in today, not yet checked out)
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :today AND a.checkOutTime IS NULL")
    long countActiveInside(@Param("today") LocalDate today);

    // Peak hour for today
    @Query(value = "SELECT HOUR(check_in_time) as hr, COUNT(*) as cnt FROM attendance WHERE attendance_date = :today GROUP BY hr ORDER BY cnt DESC LIMIT 1", nativeQuery = true)
    Optional<Object[]> findPeakHour(@Param("today") LocalDate today);
}
