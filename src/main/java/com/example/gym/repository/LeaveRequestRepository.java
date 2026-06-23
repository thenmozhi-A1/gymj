package com.example.gym.repository;

import com.example.gym.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByStaffIdOrderByAppliedAtDesc(Long staffId);
    List<LeaveRequest> findAllByOrderByAppliedAtDesc();
}
