package com.example.gym.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the user
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate attendanceDate;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    // "PRESENT" / "ABSENT"
    private String status;

    private String notes; // Optional notes by trainer/admin

    @PrePersist
    public void prePersist() {
        if (this.attendanceDate == null) this.attendanceDate = LocalDate.now();
        if (this.checkInTime == null) this.checkInTime = LocalTime.now();
        if (this.status == null) this.status = "PRESENT";
    }
}
