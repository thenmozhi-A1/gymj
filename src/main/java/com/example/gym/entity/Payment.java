package com.example.gym.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double amount;
    private String planName;
    private String paymentMethod;
    private String transactionId;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private LocalDateTime planStartDate;
    private LocalDateTime planEndDate;

    public Payment() {}

    public Payment(Long id, User user, Double amount, String planName, String paymentMethod, String transactionId, String paymentStatus, LocalDateTime paymentDate, LocalDateTime planStartDate, LocalDateTime planEndDate) {
        this.id = id;
        this.user = user;
        this.amount = amount;
        this.planName = planName;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
        this.planStartDate = planStartDate;
        this.planEndDate = planEndDate;
    }

    @PrePersist
    public void prePersist() {
        this.paymentDate = LocalDateTime.now();
        if (this.paymentStatus == null)
            this.paymentStatus = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public LocalDateTime getPlanStartDate() { return planStartDate; }
    public void setPlanStartDate(LocalDateTime planStartDate) { this.planStartDate = planStartDate; }
    public LocalDateTime getPlanEndDate() { return planEndDate; }
    public void setPlanEndDate(LocalDateTime planEndDate) { this.planEndDate = planEndDate; }
}
