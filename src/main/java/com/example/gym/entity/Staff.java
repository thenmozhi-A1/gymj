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
    private Integer leaves = 0;
    private Integer permissions = 0;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private User user;

    public Staff() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
