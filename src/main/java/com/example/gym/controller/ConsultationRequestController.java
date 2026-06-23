package com.example.gym.controller;

import com.example.gym.entity.ConsultationRequest;
import com.example.gym.repository.ConsultationRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.gym.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationRequestController {

    private final ConsultationRequestRepository repository;
    private final NotificationService notificationService;

    public ConsultationRequestController(ConsultationRequestRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    /** POST /api/consultations — Submit a new request */
    @PostMapping
    public ResponseEntity<?> submitRequest(@RequestBody ConsultationRequest request) {
        try {
            ConsultationRequest saved = repository.save(request);
            notificationService.broadcast("ENQUIRY", Map.of(
                "name", saved.getFullName() != null ? saved.getFullName() : "Someone",
                "phone", saved.getPhone() != null ? saved.getPhone() : ""
            ));
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to submit request"));
        }
    }

    /** GET /api/consultations — Get all requests (for Admin) */
    @GetMapping
    public List<ConsultationRequest> getAllRequests() {
        return repository.findAll();
    }

    /** DELETE /api/consultations/{id} — Delete a request */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequest(@PathVariable("id") Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Request deleted successfully"));
    }

    /** PUT /api/consultations/{id}/status ?" Update a request status */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        ConsultationRequest req = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        req.setStatus(payload.get("status"));
        repository.save(req);
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }
}
