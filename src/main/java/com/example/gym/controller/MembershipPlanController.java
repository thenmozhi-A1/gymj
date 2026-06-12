package com.example.gym.controller;

import com.example.gym.entity.MembershipPlan;
import com.example.gym.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership-plans")
@RequiredArgsConstructor
public class MembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    // Publicly accessible for the Subscription page
    @GetMapping
    public ResponseEntity<List<MembershipPlan>> getAllPlans() {
        return ResponseEntity.ok(membershipPlanService.getAllPlans());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembershipPlan> createPlan(@RequestBody MembershipPlan plan) {
        return ResponseEntity.ok(membershipPlanService.createPlan(plan));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembershipPlan> updatePlan(@PathVariable Long id, @RequestBody MembershipPlan plan) {
        return ResponseEntity.ok(membershipPlanService.updatePlan(id, plan));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePlan(@PathVariable Long id) {
        membershipPlanService.deletePlan(id);
        return ResponseEntity.ok().build();
    }
}
