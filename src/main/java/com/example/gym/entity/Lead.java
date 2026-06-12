package com.example.gym.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String mobile;
    private String email;
    private String source;
    private String interestedPlan;
    private LocalDate followUpDate;
    private String status;
    private Boolean converted = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_user_id")
    private User convertedUser;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
    
    private Instant updatedAt;
}
