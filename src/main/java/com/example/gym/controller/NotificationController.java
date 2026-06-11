package com.example.gym.controller;

import com.example.gym.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Exposes the SSE stream endpoint for admin real-time notifications.
 * GET /api/notifications/stream  — admin clients subscribe here via EventSource.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Admin clients open a long-lived SSE connection to this endpoint.
     * Events pushed: NEW_MEMBER, PAYMENT_FAILED, ATTENDANCE, CONSULTATION
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return notificationService.subscribe();
    }
}
