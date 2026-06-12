package com.example.gym.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // NEW_MEMBER, PAYMENT_FAILED, ATTENDANCE, EXPIRY_ALERT, LEAD_FOLLOWUP, LOW_STOCK

    @Column(columnDefinition = "JSON")
    private String payload;

    private Boolean isRead = false;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
}
