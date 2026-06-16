package com.example.gym.controller;

import com.example.gym.entity.MembershipPlan;
import com.example.gym.repository.MembershipPlanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership-plans")
public class MembershipPlanController {

    private final MembershipPlanRepository membershipPlanRepository;

    public MembershipPlanController(MembershipPlanRepository membershipPlanRepository) {
        this.membershipPlanRepository = membershipPlanRepository;
    }

    @GetMapping
    public List<MembershipPlan> getAllPlans() {
        return membershipPlanRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<MembershipPlan> createPlan(@RequestBody MembershipPlan plan) {
        MembershipPlan saved = membershipPlanRepository.save(plan);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MembershipPlan> updatePlan(@PathVariable Long id, @RequestBody MembershipPlan planDetails) {
        return membershipPlanRepository.findById(id)
                .map(existingPlan -> {
                    existingPlan.setTitle(planDetails.getTitle());
                    existingPlan.setPrice(planDetails.getPrice());
                    existingPlan.setDuration(planDetails.getDuration());
                    existingPlan.setBadge(planDetails.getBadge());
                    existingPlan.setIsPopular(planDetails.getIsPopular());
                    existingPlan.setIsPremium(planDetails.getIsPremium());
                    existingPlan.setFeatures(planDetails.getFeatures());
                    existingPlan.setImageUrl(planDetails.getImageUrl());
                    existingPlan.setAccentColor(planDetails.getAccentColor());
                    existingPlan.setRating(planDetails.getRating());
                    existingPlan.setUserCount(planDetails.getUserCount());
                    existingPlan.setBonus(planDetails.getBonus());
                    MembershipPlan updated = membershipPlanRepository.save(existingPlan);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable Long id) {
        return membershipPlanRepository.findById(id)
                .map(existingPlan -> {
                    membershipPlanRepository.delete(existingPlan);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
