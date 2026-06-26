package com.example.gym.service;

import com.example.gym.config.WhatsAppConfig;
import com.example.gym.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WhatsAppService
 *
 * Sends messages via the Meta WhatsApp Business Cloud API.
 * All credentials are loaded from the WhatsAppConfig (environment variables).
 *
 * Key behaviours:
 *   - Returns silently when whatsapp.enabled=false (safe to call without credentials).
 *   - Phone numbers are normalised to digits-only international format.
 *   - API errors are caught, logged, and never re-thrown — WhatsApp failures
 *     will not disrupt the email notification flow.
 */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    private final WhatsAppConfig config;
    private final RestTemplate restTemplate;

    public WhatsAppService(WhatsAppConfig config, RestTemplateBuilder builder) {
        this.config       = config;
        this.restTemplate = builder.build();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Sends a free-form text message.
     * <p>
     * Note: Text messages only work within the 24-hour customer-service window
     * (i.e., after the user has messaged you first). To initiate a conversation,
     * use {@link #sendTemplateMessage} with a pre-approved template instead.
     *
     * @param to   recipient phone number (any format — will be normalised)
     * @param text the message body
     */
    public void sendTextMessage(String to, String text) {
        if (!config.isEnabled()) {
            log.info("[WhatsApp] Disabled — skipping text message to {}", maskPhone(to));
            return;
        }

        String normalisedTo = normalisePhone(to);
        String url = buildMessagesUrl();

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", normalisedTo,
                "type", "text",
                "text", Map.of("body", text)
        );

        post(url, body, normalisedTo, "text");
    }

    /**
     * Sends a pre-approved template message (required to initiate conversations).
     * Templates must be created and approved in Meta Business Manager first.
     *
     * @param to           recipient phone number
     * @param templateName the template name as registered in Meta
     * @param languageCode language code (e.g., "en_US", "en")
     * @param parameters   positional parameters for the template body
     */
    public void sendTemplateMessage(String to, String templateName,
                                    String languageCode, List<String> parameters) {
        if (!config.isEnabled()) {
            log.info("[WhatsApp] Disabled — skipping template '{}' to {}", templateName, maskPhone(to));
            return;
        }

        String normalisedTo = normalisePhone(to);
        String url = buildMessagesUrl();

        List<Map<String, String>> bodyParams = parameters.stream()
                .map(p -> Map.of("type", "text", "text", p))
                .collect(Collectors.toList());

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", normalisedTo,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", languageCode),
                        "components", List.of(
                                Map.of("type", "body", "parameters", bodyParams)
                        )
                )
        );

        post(url, body, normalisedTo, "template:" + templateName);
    }

    /**
     * Convenience method — sends a membership expiry reminder via WhatsApp.
     * Falls back gracefully if the user has no phone number on file.
     */
    public void sendExpiryReminder(User user, String planName, String expiryDate, int daysLeft) {
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            log.debug("[WhatsApp] No phone number for user {} — skipping", user.getId());
            return;
        }

        String recipientName = user.getFullName() != null ? user.getFullName() : "Member";
        String gymName = "B&Y Fitness Gym";

        String message = String.format(
                "Hi %s! \uD83C\uDFCB\uFE0F Your *%s* membership at *%s* expires on *%s* (%d day%s left). " +
                "Renew now to keep your fitness streak going! \uD83D\uDCAA",
                recipientName, planName, gymName, expiryDate, daysLeft,
                daysLeft == 1 ? "" : "s"
        );

        sendTextMessage(user.getPhone(), message);
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private String buildMessagesUrl() {
        return config.getApiUrl() + "/" + config.getPhoneNumberId() + "/messages";
    }

    private void post(String url, Map<String, Object> body, String to, String label) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(config.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.info("[WhatsApp] {} sent to {}. Status: {}", label, maskPhone(to), response.getStatusCode());
        } catch (Exception e) {
            log.error("[WhatsApp] Failed to send {} to {}: {}", label, maskPhone(to), e.getMessage());
        }
    }

    /**
     * Strips all non-digit characters so numbers like "+91 98765-43210" become "919876543210".
     */
    private String normalisePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^\\d]", "");
    }

    /**
     * Masks phone numbers in log output for privacy (shows last 4 digits only).
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        String digits = normalisePhone(phone);
        return "***" + digits.substring(digits.length() - 4);
    }
}
