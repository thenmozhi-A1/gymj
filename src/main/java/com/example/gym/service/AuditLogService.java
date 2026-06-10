package com.example.gym.service;

import com.example.gym.entity.AuditLog;
import com.example.gym.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AuditLogService — creates and retrieves audit log entries.
 *
 * Call {@link #log(String, String, Long, String, String)} from any controller
 * after a successful admin mutation.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository repo;

    public AuditLogService(AuditLogRepository repo) {
        this.repo = repo;
    }

    /**
     * Persist a new audit entry.
     *
     * @param action     Short constant, e.g. "DELETE_USER", "ADD_STAFF"
     * @param adminEmail Email of the acting admin (from SecurityContext)
     * @param targetId   ID of the affected entity (may be null)
     * @param targetType Human-readable entity type, e.g. "User", "Payment"
     * @param details    Free-text description, e.g. "Deleted member John Doe"
     */
    public AuditLog log(String action, String adminEmail, Long targetId, String targetType, String details) {
        AuditLog entry = new AuditLog();
        entry.setAction(action);
        entry.setAdminEmail(adminEmail != null ? adminEmail : "system");
        entry.setTargetId(targetId);
        entry.setTargetType(targetType);
        entry.setDetails(details);
        return repo.save(entry);
    }

    /** All logs, newest first — used by the admin Reports view */
    public List<AuditLog> getAll() {
        return repo.findAllByOrderByTimestampDesc();
    }

    /** Logs filtered by admin email */
    public List<AuditLog> getByAdmin(String email) {
        return repo.findByAdminEmailOrderByTimestampDesc(email);
    }

    /** Logs filtered by action type */
    public List<AuditLog> getByAction(String action) {
        return repo.findByActionOrderByTimestampDesc(action);
    }
}
