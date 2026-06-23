package com.example.gym.controller;

import com.example.gym.entity.WorkoutPlan;
import com.example.gym.repository.WorkoutPlanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutPlanController {

    private final WorkoutPlanRepository repository;

    public WorkoutPlanController(WorkoutPlanRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<WorkoutPlan> getAllWorkoutPlans() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createWorkoutPlan(@RequestBody WorkoutPlan plan) {
        WorkoutPlan saved = repository.save(plan);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkoutPlan(@PathVariable("id") Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Workout plan deleted successfully"));
    }
}
