package com.example.gym.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "membership_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String price;

    @Column(nullable = false)
    private String duration;

    private String badge;

    @Column(name = "is_popular")
    private Boolean isPopular;

    @Column(name = "is_premium")
    private Boolean isPremium;

    private Double rating;

    @Column(name = "user_count")
    private String userCount;

    @Column(name = "image_url")
    private String imageUrl;

    private String bonus;

    @Column(name = "accent_color")
    private String accentColor;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "membership_plan_features", joinColumns = @JoinColumn(name = "membership_plan_id"))
    @Column(name = "feature")
    private List<String> features;
}
