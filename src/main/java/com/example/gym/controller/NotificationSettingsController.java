package com.example.gym.controller;

import com.example.gym.config.NotificationSettings;
import com.example.gym.service.ExpiryNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * NotificationSettingsController
 *
 * GET  /api/notification-settings           — returns current settings
 * PUT  /api/notification-settings           — updates enabled flag and days-ahead
 * POST /api/notification-settings/test-run  — triggers a manual expiry scan now
 */
@RestController
@RequestMapping("/api/notification-settings")
public class NotificationSettingsController {

    private final NotificationSettings settings;
    private final ExpiryNotificationService expiryService;

    public NotificationSettingsController(NotificationSettings settings,
                                          ExpiryNotificationService expiryService) {
        this.settings      = settings;
        this.expiryService = expiryService;
    }

    /** Return current notification config */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        return ResponseEntity.ok(Map.of(
                "enabled",         settings.isEnabled(),
                "expiryDaysAhead", settings.getExpiryDaysAhead(),
                "fromEmail",       settings.getFromEmail(),
                "gymName",         settings.getGymName()
        ));
    }

    /**
     * Update settings at runtime (no restart needed).
     * Body: { "enabled": true, "expiryDaysAhead": 7 }
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody Map<String, Object> body) {
        if (body.containsKey("enabled")) {
            settings.setEnabled(Boolean.parseBoolean(body.get("enabled").toString()));
        }
        if (body.containsKey("expiryDaysAhead")) {
            settings.setExpiryDaysAhead(Integer.parseInt(body.get("expiryDaysAhead").toString()));
        }
        if (body.containsKey("fromEmail")) {
            settings.setFromEmail(body.get("fromEmail").toString());
        }
        return ResponseEntity.ok(Map.of(
                "message",         "Settings updated",
                "enabled",         settings.isEnabled(),
                "expiryDaysAhead", settings.getExpiryDaysAhead()
        ));
    }

    /** Manually fire the expiry scan immediately (admin-only) */
    @PostMapping("/test-run")
    public ResponseEntity<Map<String, Object>> testRun() {
        expiryService.triggerManualRun();
        return ResponseEntity.ok(Map.of(
                "message", "Expiry notification scan triggered. Check server logs for details."
        ));
    }
}
