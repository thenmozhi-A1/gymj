package com.example.gym.service;

import com.example.gym.entity.MembershipPlan;
import com.example.gym.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipPlanService {

    private final MembershipPlanRepository membershipPlanRepository;

    public List<MembershipPlan> getAllPlans() {
        return membershipPlanRepository.findAll();
    }

    public MembershipPlan createPlan(MembershipPlan plan) {
        if (plan.getFeatures() == null) {
            plan.setFeatures(new java.util.ArrayList<>());
        }
        return membershipPlanRepository.save(plan);
    }

    public MembershipPlan updatePlan(Long id, MembershipPlan planDetails) {
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        plan.setTitle(planDetails.getTitle());
        plan.setPrice(planDetails.getPrice());
        plan.setDuration(planDetails.getDuration());
        plan.setBadge(planDetails.getBadge());
        plan.setIsPopular(planDetails.getIsPopular());
        plan.setIsPremium(planDetails.getIsPremium());
        plan.setRating(planDetails.getRating());
        plan.setUserCount(planDetails.getUserCount());
        plan.setImageUrl(planDetails.getImageUrl());
        plan.setBonus(planDetails.getBonus());
        plan.setAccentColor(planDetails.getAccentColor());
        
        if (planDetails.getFeatures() != null) {
            if (plan.getFeatures() == null) {
                plan.setFeatures(new java.util.ArrayList<>());
            }
            plan.getFeatures().clear();
            plan.getFeatures().addAll(planDetails.getFeatures());
        }

        return membershipPlanRepository.save(plan);
    }

    public void deletePlan(Long id) {
        membershipPlanRepository.deleteById(id);
    }
}
