package com.example.gym.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category; // SUPPLEMENT, APPAREL, EQUIPMENT, ACCESSORY

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(unique = true)
    private String sku;

    private String imageUrl;

    private Boolean isActive = true;

    @Version
    private Long version; // Optimistic locking to prevent oversell

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}
