package com.example.gym.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "product_orders")
@Data
@NoArgsConstructor
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String orderStatus; // PENDING, CONFIRMED, DISPATCHED, DELIVERED, CANCELLED

    @Column(nullable = false)
    private LocalDate orderDate = LocalDate.now();

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}
