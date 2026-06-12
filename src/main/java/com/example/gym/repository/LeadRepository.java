package com.example.gym.repository;

import com.example.gym.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByFollowUpDate(LocalDate date);
}
