package com.example.gym.controller;

import com.example.gym.entity.ConsultationRequest;
import com.example.gym.repository.ConsultationRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationRequestController {

    private final ConsultationRequestRepository repository;

    public ConsultationRequestController(ConsultationRequestRepository repository) {
        this.repository = repository;
    }

    /** POST /api/consultations — Submit a new request */
    @PostMapping
    public ResponseEntity<?> submitRequest(@RequestBody ConsultationRequest request) {
        try {
            ConsultationRequest saved = repository.save(request);
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
    public ResponseEntity<?> deleteRequest(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Request deleted successfully"));
    }
}
