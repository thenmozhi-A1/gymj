package com.example.gym.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the user who made the payment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double amount;

    private String planName; // e.g. "Monthly - Basic", "Yearly - Premium"

    private String paymentMethod; // e.g. "UPI", "Card", "Cash"

    private String transactionId;

    // "SUCCESS" / "PENDING" / "FAILED"
    private String paymentStatus;

    private LocalDateTime paymentDate;

    private LocalDateTime planStartDate;

    private LocalDateTime planEndDate;

    @PrePersist
    public void prePersist() {
        this.paymentDate = LocalDateTime.now();
        if (this.paymentStatus == null)
            this.paymentStatus = "PENDING";
    }
}
