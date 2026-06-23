package com.example.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diet_plans")
public class DietPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int calories;
    private int protein;
    private int carbs;
    private int fats;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private int assignedMembersCount = 0;
    
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public int getProtein() { return protein; }
    public void setProtein(int protein) { this.protein = protein; }
    public int getCarbs() { return carbs; }
    public void setCarbs(int carbs) { this.carbs = carbs; }
    public int getFats() { return fats; }
    public void setFats(int fats) { this.fats = fats; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getAssignedMembersCount() { return assignedMembersCount; }
    public void setAssignedMembersCount(int assignedMembersCount) { this.assignedMembersCount = assignedMembersCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
