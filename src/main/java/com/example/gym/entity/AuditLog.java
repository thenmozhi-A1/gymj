package com.example.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit log entry — records every significant admin mutation.
 * Fields: action, adminEmail, targetId, targetType, details, timestamp.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_admin",     columnList = "adminEmail"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action",    columnList = "action")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Category of the action, e.g. DELETE_USER, ADD_STAFF, PAYMENT_UPDATE */
    @Column(nullable = false, length = 100)
    private String action;

    /** Email of the admin who performed the action */
    @Column(nullable = false, length = 255)
    private String adminEmail;

    /** ID of the entity that was affected (userId, staffId, paymentId …) */
    @Column
    private Long targetId;

    /** Human-readable entity type for display, e.g. "User", "Staff", "Payment" */
    @Column(length = 50)
    private String targetType;

    /** Optional free-text details, e.g. "Deleted user John Doe (id=42)" */
    @Column(length = 1000)
    private String details;

    /** When this action occurred (UTC) */
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId()            { return id; }

    public String getAction()      { return action; }
    public void setAction(String a){ this.action = a; }

    public String getAdminEmail()           { return adminEmail; }
    public void setAdminEmail(String email) { this.adminEmail = email; }

    public Long getTargetId()         { return targetId; }
    public void setTargetId(Long tid) { this.targetId = tid; }

    public String getTargetType()            { return targetType; }
    public void setTargetType(String type)   { this.targetType = type; }

    public String getDetails()           { return details; }
    public void setDetails(String d)     { this.details = d; }

    public LocalDateTime getTimestamp()           { return timestamp; }
    public void setTimestamp(LocalDateTime ts)    { this.timestamp = ts; }
}
