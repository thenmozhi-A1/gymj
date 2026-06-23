package com.example.gym.controller;

import com.example.gym.entity.DietPlan;
import com.example.gym.repository.DietPlanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diets")
public class DietPlanController {

    private final DietPlanRepository repository;

    public DietPlanController(DietPlanRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<DietPlan> getAllDietPlans() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createDietPlan(@RequestBody DietPlan plan) {
        DietPlan saved = repository.save(plan);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDietPlan(@PathVariable("id") Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Diet plan deleted successfully"));
    }
}
