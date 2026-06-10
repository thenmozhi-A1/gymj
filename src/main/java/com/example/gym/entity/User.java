package com.example.gym.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;
    private String address;
    private String gender;
    private String membershipType;
    private String status;
    private String role;

    @Column(unique = true)
    private String fingerprintHash;
    private Boolean fingerprintEnrolled = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private Staff staffDetails;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Password security fields
    private Integer failedLoginAttempts = 0;
    private LocalDateTime lockedUntil;
    private Boolean mustChangePassword = false;
    private LocalDateTime passwordChangedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<Payment> payments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<Attendance> attendances;

    public User() {}

    public User(Long id, String fullName, String email, String password, String phone, String address, String gender, String membershipType, String status, String role, String fingerprintHash, Boolean fingerprintEnrolled, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.gender = gender;
        this.membershipType = membershipType;
        this.status = status;
        this.role = role;
        this.fingerprintHash = fingerprintHash;
        this.fingerprintEnrolled = fingerprintEnrolled;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "ACTIVE";
        if (this.role == null) this.role = "USER";
        if (this.fingerprintEnrolled == null) this.fingerprintEnrolled = false;
        if (this.failedLoginAttempts == null) this.failedLoginAttempts = 0;
        if (this.mustChangePassword == null) this.mustChangePassword = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getMembershipType() { return membershipType; }
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Staff getStaffDetails() { return staffDetails; }
    public void setStaffDetails(Staff staffDetails) { this.staffDetails = staffDetails; }
    public String getFingerprintHash() { return fingerprintHash; }
    public void setFingerprintHash(String fingerprintHash) { this.fingerprintHash = fingerprintHash; }
    public Boolean getFingerprintEnrolled() { return fingerprintEnrolled; }
    public void setFingerprintEnrolled(Boolean fingerprintEnrolled) { this.fingerprintEnrolled = fingerprintEnrolled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Security Getters and Setters
    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    public Boolean getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
}
