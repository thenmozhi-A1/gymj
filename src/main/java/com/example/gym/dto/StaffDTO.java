package com.example.gym.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StaffDTO {

    private Long id;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    private String password;

    private String phone;
    private String address;
    private String role;
    private String status;
    private String fingerprintHash;
    private Boolean fingerprintEnrolled;

    // Staff-specific details
    private String salary;
    private String times;
    private String specialty;
    private Integer leaves;
    private Integer permissions;

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
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFingerprintHash() { return fingerprintHash; }
    public void setFingerprintHash(String fingerprintHash) { this.fingerprintHash = fingerprintHash; }
    public Boolean getFingerprintEnrolled() { return fingerprintEnrolled; }
    public void setFingerprintEnrolled(Boolean fingerprintEnrolled) { this.fingerprintEnrolled = fingerprintEnrolled; }

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
}
