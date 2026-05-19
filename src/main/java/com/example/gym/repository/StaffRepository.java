package com.example.gym.repository;

import com.example.gym.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmail(String email);
    java.util.List<Staff> findByFingerprintHash(String fingerprintHash);
    boolean existsByEmail(String email);
}
