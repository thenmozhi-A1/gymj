package com.example.gym.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "staff_details")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String salary;
    private String times;
    private String specialty;
    private String experience;
    private Integer leaves = 0;
    private Integer permissions = 0;

    private String department;
    private java.time.LocalDate joiningDate;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    @Column(columnDefinition = "TEXT")
    private String documents;
    
    @Column(columnDefinition = "LONGTEXT")
    private String profilePhoto;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<LeaveRequest> leaveRequests;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Attendance> attendances;

    public Staff() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
    
    public String getTimes() { return times; }
    public void setTimes(String times) { this.times = times; }
    
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    
    public Integer getLeaves() { return leaves; }
    public void setLeaves(Integer leaves) { this.leaves = leaves; }
    
    public Integer getPermissions() { return permissions; }
    public void setPermissions(Integer permissions) { this.permissions = permissions; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public java.time.LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(java.time.LocalDate joiningDate) { this.joiningDate = joiningDate; }
    
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
    
    public String getDocuments() { return documents; }
    public void setDocuments(String documents) { this.documents = documents; }
    
    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @com.fasterxml.jackson.annotation.JsonProperty("userId")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}
