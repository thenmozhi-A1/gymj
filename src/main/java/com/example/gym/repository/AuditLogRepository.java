package com.example.gym.repository;

import com.example.gym.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Return all logs ordered newest-first */
    List<AuditLog> findAllByOrderByTimestampDesc();

    /** Filter by admin email, newest-first */
    List<AuditLog> findByAdminEmailOrderByTimestampDesc(String adminEmail);

    /** Filter by action category, newest-first */
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
}
