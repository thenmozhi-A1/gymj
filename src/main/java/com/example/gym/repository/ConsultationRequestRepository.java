package com.example.gym.repository;

import com.example.gym.entity.ConsultationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, Long> {
}
