package com.example.gym.config;

import com.example.gym.entity.MembershipPlan;
import com.example.gym.repository.MembershipPlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initDatabase(MembershipPlanRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                System.out.println("Seeding default Membership Plans...");
                
                List<MembershipPlan> plans = Arrays.asList(
                    MembershipPlan.builder()
                        .title("Standard Plan")
                        .price("5000")
                        .duration("Per Month")
                        .badge("Budget Friendly")
                        .rating(4.5)
                        .userCount("5k+ Members")
                        .imageUrl("https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=1470&auto=format&fit=crop")
                        .bonus("7-Day Money Back Guarantee")
                        .accentColor("#3b82f6")
                        .features(Arrays.asList("Access during Peak Hours", "Basic Workout Routines", "Standard Gym Equipment", "Locker Room Access", "Free Hydration Station", "Online Support Community"))
                        .build(),
                    MembershipPlan.builder()
                        .title("Pro Membership")
                        .price("9000")
                        .duration("Per 6 Months")
                        .badge("Most Popular")
                        .rating(4.8)
                        .userCount("2.5k+ Members")
                        .imageUrl("https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5?q=80&w=1470&auto=format&fit=crop")
                        .bonus("10% Discount on Supplements")
                        .accentColor("#f97316")
                        .features(Arrays.asList("Full Access (6 AM - Midnight)", "4 PT Sessions per Month", "Standard Nutritional Guide", "Locker & Shower Facilities", "Access to Yoga & HIIT Classes", "Monthly Body Scan Analysis"))
                        .build(),
                    MembershipPlan.builder()
                        .title("Elite Yearly")
                        .price("12000")
                        .duration("Per Year")
                        .badge("Best Value")
                        .rating(5.0)
                        .userCount("800+ Members")
                        .imageUrl("https://images.unsplash.com/photo-1593079831268-3381b0db4a77?q=80&w=1469&auto=format&fit=crop")
                        .bonus("Includes Free Gym Apparel")
                        .accentColor("#ef4444")
                        .features(Arrays.asList("24/7 Access to All Gyms", "Unlimited Personal Training", "Customized Macro Plans", "Spa & Recovery Zone", "Free Supplement Monthly Kit", "Biometric Health Tracking"))
                        .build(),
                    MembershipPlan.builder()
                        .title("VIP Yearly")
                        .price("18000")
                        .duration("Per Year")
                        .badge("Ultimate Experience")
                        .rating(5.0)
                        .userCount("300+ Members")
                        .imageUrl("https://images.unsplash.com/photo-1540497077202-7c8a3999166f?q=80&w=1470&auto=format&fit=crop")
                        .bonus("VIP Event Invitations")
                        .accentColor("#ffc107")
                        .features(Arrays.asList("Everything in Elite Plan", "Personal Nutritionist", "Home Workout Equipment Hire", "Monthly Massage Therapy", "Guest Pass for Friends", "Private Locker with Name"))
                        .build(),
                    MembershipPlan.builder()
                        .title("Custom Plan")
                        .price("Custom")
                        .duration("Flexible")
                        .badge("For Teams/Groups")
                        .rating(4.9)
                        .userCount("50+ Corporate Teams")
                        .imageUrl("https://images.unsplash.com/photo-1571902943202-507ec2618e8f?q=80&w=1375&auto=format&fit=crop")
                        .bonus("Dedicated Account Manager")
                        .accentColor("#a855f7")
                        .features(Arrays.asList("Tailored Group Sessions", "Corporate Wellness Programs", "Custom Training Modules", "Flexible Timing Slots", "Team Progress Reports", "Special Event Hosting"))
                        .build()
                );
                
                repository.saveAll(plans);
                System.out.println("Successfully seeded default Membership Plans.");
            }
        };
    }
}
