package com.example.gym.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_id", nullable = true)
    private Staff staff;

    private LocalDate attendanceDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String status;
    private String notes;

    private Boolean isLate = false;
    private String correctionReason;
    private String correctionStatus; // NONE, PENDING, APPROVED, REJECTED

    public Attendance() {}

    public Attendance(Long id, User user, LocalDate attendanceDate, LocalTime checkInTime, LocalTime checkOutTime, String status, String notes) {
        this.id = id;
        this.user = user;
        this.attendanceDate = attendanceDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.notes = notes;
    }

    @PrePersist
    public void prePersist() {
        if (this.attendanceDate == null) this.attendanceDate = LocalDate.now();
        if (this.checkInTime == null) this.checkInTime = LocalTime.now();
        if (this.status == null) this.status = "PRESENT";
        if (this.isLate == null) this.isLate = false;
        if (this.correctionStatus == null) this.correctionStatus = "NONE";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Staff getStaff() { return staff; }
    public void setStaff(Staff staff) { this.staff = staff; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }
    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Boolean getIsLate() { return isLate; }
    public void setIsLate(Boolean isLate) { this.isLate = isLate; }
    public String getCorrectionReason() { return correctionReason; }
    public void setCorrectionReason(String correctionReason) { this.correctionReason = correctionReason; }
    public String getCorrectionStatus() { return correctionStatus; }
    public void setCorrectionStatus(String correctionStatus) { this.correctionStatus = correctionStatus; }
}
