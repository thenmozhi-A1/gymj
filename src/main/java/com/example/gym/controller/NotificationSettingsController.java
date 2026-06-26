package com.example.gym.controller;

import com.example.gym.config.NotificationSettings;
import com.example.gym.config.WhatsAppConfig;
import com.example.gym.service.ExpiryNotificationService;
import com.example.gym.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * NotificationSettingsController
 *
 * GET  /api/notification-settings              — returns current settings (email + WhatsApp)
 * PUT  /api/notification-settings              — updates enabled flags and days-ahead
 * POST /api/notification-settings/test-run     — triggers a manual expiry scan now
 * POST /api/notification-settings/test-whatsapp — sends a test WhatsApp message
 */
@RestController
@RequestMapping("/api/notification-settings")
public class NotificationSettingsController {

    private final NotificationSettings settings;
    private final WhatsAppConfig whatsAppConfig;
    private final ExpiryNotificationService expiryService;
    private final WhatsAppService whatsAppService;

    public NotificationSettingsController(NotificationSettings settings,
                                          WhatsAppConfig whatsAppConfig,
                                          ExpiryNotificationService expiryService,
                                          WhatsAppService whatsAppService) {
        this.settings       = settings;
        this.whatsAppConfig = whatsAppConfig;
        this.expiryService  = expiryService;
        this.whatsAppService = whatsAppService;
    }

    /** Return current notification config (email + WhatsApp) */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", settings.isEnabled());
        response.put("expiryDaysAhead", settings.getExpiryDaysAhead());
        response.put("fromEmail", settings.getFromEmail());
        response.put("gymName", settings.getGymName());
        response.put("whatsappEnabled", whatsAppConfig.isEnabled());
        return ResponseEntity.ok(response);
    }

    /**
     * Update settings at runtime (no restart needed).
     * Body: { "enabled": true, "expiryDaysAhead": 7, "whatsappEnabled": true }
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
        if (body.containsKey("whatsappEnabled")) {
            whatsAppConfig.setEnabled(Boolean.parseBoolean(body.get("whatsappEnabled").toString()));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Settings updated");
        response.put("enabled", settings.isEnabled());
        response.put("expiryDaysAhead", settings.getExpiryDaysAhead());
        response.put("whatsappEnabled", whatsAppConfig.isEnabled());
        return ResponseEntity.ok(response);
    }

    /** Manually fire the expiry scan immediately (admin-only) */
    @PostMapping("/test-run")
    public ResponseEntity<Map<String, Object>> testRun() {
        expiryService.triggerManualRun();
        return ResponseEntity.ok(Map.of(
                "message", "Expiry notification scan triggered. Check server logs for details."
        ));
    }

    /**
     * Send a test WhatsApp message to a given phone number.
     * Body: { "phone": "919876543210" }
     */
    @PostMapping("/test-whatsapp")
    public ResponseEntity<Map<String, Object>> testWhatsApp(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Phone number is required in the request body"
            ));
        }

        if (!whatsAppConfig.isEnabled()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "WhatsApp is disabled. Enable it first via PUT /api/notification-settings"
            ));
        }

        whatsAppService.sendTextMessage(phone,
                "\u2705 This is a test message from *" + settings.getGymName() +
                "* \uD83C\uDFCB\uFE0F\nYour WhatsApp integration is working correctly!");

        return ResponseEntity.ok(Map.of(
                "message", "Test WhatsApp message sent. Check your phone and server logs."
        ));
    }
}
