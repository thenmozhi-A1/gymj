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

    // Staff-specific fields
    private String salary;
    private String times;
    private String specialty;
    private Integer leaves = 0;
    private Integer permissions = 0;
    @Column(unique = true)
    private String fingerprintHash;
    private Boolean fingerprintEnrolled = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<Payment> payments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<Attendance> attendances;

    public User() {}

    public User(Long id, String fullName, String email, String password, String phone, String address, String gender, String membershipType, String status, String role, String salary, String times, String specialty, Integer leaves, Integer permissions, String fingerprintHash, Boolean fingerprintEnrolled, LocalDateTime createdAt) {
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
        this.salary = salary;
        this.times = times;
        this.specialty = specialty;
        this.leaves = leaves;
        this.permissions = permissions;
        this.fingerprintHash = fingerprintHash;
        this.fingerprintEnrolled = fingerprintEnrolled;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "ACTIVE";
        if (this.role == null) this.role = "USER";
        if (this.leaves == null) this.leaves = 0;
        if (this.permissions == null) this.permissions = 0;
        if (this.fingerprintEnrolled == null) this.fingerprintEnrolled = false;
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
    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
    public String getTimes() { return times; }
    public void setTimes(String times) { this.times = times; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public Integer getLeaves() { return leaves; }
    public void setLeaves(Integer leaves) { this.leaves = leaves; }
    public Integer getPermissions() { return permissions; }
    public void setPermissions(Integer permissions) { this.permissions = permissions; }
    public String getFingerprintHash() { return fingerprintHash; }
    public void setFingerprintHash(String fingerprintHash) { this.fingerprintHash = fingerprintHash; }
    public Boolean getFingerprintEnrolled() { return fingerprintEnrolled; }
    public void setFingerprintEnrolled(Boolean fingerprintEnrolled) { this.fingerprintEnrolled = fingerprintEnrolled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
