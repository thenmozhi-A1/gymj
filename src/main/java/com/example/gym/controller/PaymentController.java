package com.example.gym.controller; 
import com.example.gym.entity.Payment;
import com.example.gym.service.PaymentService;
import com.example.gym.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final NotificationService notificationService;

    public PaymentController(PaymentService paymentService, NotificationService notificationService) {
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }

    /** POST /api/payments/user/{userId} — Add payment for a user */
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> addPayment(@PathVariable Long userId, @RequestBody Payment payment) {
        try {
            Payment saved = paymentService.addPayment(userId, payment);
            // Notify admins if the payment failed
            if ("FAILED".equalsIgnoreCase(saved.getPaymentStatus())) {
                notificationService.broadcast("PAYMENT_FAILED", Map.of(
                        "id", saved.getId(),
                        "amount", saved.getAmount() != null ? saved.getAmount() : 0,
                        "user", userId
                ));
            }
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /api/payments — Get all payments (admin) */
    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    /** GET /api/payments/user/{userId} — Get payments by user */
    @GetMapping("/user/{userId}")
    public List<Payment> getPaymentsByUser(@PathVariable Long userId) {
        return paymentService.getPaymentsByUser(userId);
    }

    /** GET /api/payments/{id} — Get payment by ID */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** PATCH /api/payments/{id}/status — Update payment status */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(paymentService.updatePaymentStatus(id, body.get("status")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** DELETE /api/payments/{id} — Delete payment record */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(Map.of("message", "Payment deleted successfully"));
    }
}
