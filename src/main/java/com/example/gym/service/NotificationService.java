package com.example.gym.service;

import com.example.gym.entity.Notification;
import com.example.gym.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages SSE (Server-Sent Events) connections for real-time admin notifications.
 * Keeps a thread-safe list of active emitters and broadcasts events to all of them.
 */
@Service
public class NotificationService {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository notificationRepository, ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new SSE emitter, registers it, and sets up lifecycle callbacks to remove
     * it when the connection is closed or times out.
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // Send a "connected" ping so the client can confirm the stream is open
        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data("stream-open"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    /**
     * Broadcasts a notification event to all currently subscribed admin clients.
     * Also saves the notification to the database.
     *
     * @param type    Event category (e.g. "NEW_MEMBER", "PAYMENT_FAILED", "ATTENDANCE")
     * @param payload A map of key-value pairs to include in the event data
     */
    public void broadcast(String type, Map<String, Object> payload) {
        // Save to DB
        try {
            Notification notification = new Notification();
            notification.setType(type);
            notification.setPayload(objectMapper.writeValueAsString(payload));
            notificationRepository.save(notification);
        } catch (Exception e) {
            // Log but don't fail broadcast
            System.err.println("Failed to save notification: " + e.getMessage());
        }

        Map<String, Object> event = Map.of(
                "type", type,
                "payload", payload,
                "timestamp", System.currentTimeMillis()
        );

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(type).data(event));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
}
