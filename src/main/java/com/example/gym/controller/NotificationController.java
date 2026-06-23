package com.example.gym.controller;

import com.example.gym.entity.User;
import com.example.gym.service.NotificationService;
import com.example.gym.service.NotificationTicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationTicketService ticketService;

    public NotificationController(NotificationService notificationService, NotificationTicketService ticketService) {
        this.notificationService = notificationService;
        this.ticketService = ticketService;
    }

    @PostMapping("/ticket")
    public ResponseEntity<?> getTicket() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        User user = (User) auth.getPrincipal();
        String ticket = ticketService.generateTicket(user);
        return ResponseEntity.ok(Map.of("ticket", ticket));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(value = "ticket", required = false) String ticket) {
        User user = ticketService.validateAndConsumeTicket(ticket);
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("Unauthorized"));
            return emitter;
        }
        return notificationService.subscribe();
    }
}
