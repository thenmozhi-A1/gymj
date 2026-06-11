package com.example.gym.controller;

import com.example.gym.entity.AuditLog;
import com.example.gym.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes audit log data to the admin frontend.
 *
 * GET /api/audit-log            — all entries, newest first
 * GET /api/audit-log?admin=x    — filter by admin email
 * GET /api/audit-log?action=x   — filter by action type
 */
@RestController
@RequestMapping("/api/audit-log")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getLogs(
            @RequestParam(required = false) String admin,
            @RequestParam(required = false) String action) {

        if (admin  != null && !admin.isBlank())  return ResponseEntity.ok(auditLogService.getByAdmin(admin));
        if (action != null && !action.isBlank()) return ResponseEntity.ok(auditLogService.getByAction(action));
        return ResponseEntity.ok(auditLogService.getAll());
    }
}
