package com.example.gym.controller;

import com.example.gym.entity.Feedback;
import com.example.gym.repository.FeedbackRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.gym.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    private final FeedbackRepository repository;
    private final NotificationService notificationService;

    public FeedbackController(FeedbackRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    /** POST /api/feedbacks — Submit a new feedback from user */
    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody Feedback feedback) {
        try {
            Feedback saved = repository.save(feedback);
            notificationService.broadcast("FEEDBACK", Map.of(
                "name", saved.getUserName() != null ? saved.getUserName() : "Anonymous",
                "message", saved.getMessage() != null ? saved.getMessage() : ""
            ));
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to submit feedback: " + e.getMessage()));
        }
    }

    /** GET /api/feedbacks — Get all feedbacks (for Admin) */
    @GetMapping
    public List<Feedback> getAllFeedbacks() {
        return repository.findAll();
    }

    /** DELETE /api/feedbacks/{id} — Delete a feedback (for Admin) */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable("id") Long id) {
        try {
            repository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Feedback deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete feedback"));
        }
    }
}
