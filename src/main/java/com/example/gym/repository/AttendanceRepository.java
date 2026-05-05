package com.example.gym.repository;

import com.example.gym.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUserId(Long userId);
    List<Attendance> findByUserIdAndAttendanceDate(Long userId, LocalDate date);
    List<Attendance> findByAttendanceDate(LocalDate date);
}
