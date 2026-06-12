package com.example.gym.scheduler;

import com.example.gym.entity.Lead;
import com.example.gym.repository.LeadRepository;
import com.example.gym.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class LeadFollowUpScheduler {

    private final LeadRepository leadRepository;
    private final NotificationService notificationService;

    public LeadFollowUpScheduler(LeadRepository leadRepository, NotificationService notificationService) {
        this.leadRepository = leadRepository;
        this.notificationService = notificationService;
    }

    /**
     * Runs every day at 08:00 AM server time.
     * Scans leads where followUpDate = LocalDate.now() and pushes SSE notification.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void pushLeadFollowUps() {
        LocalDate today = LocalDate.now();
        List<Lead> leadsToFollowUp = leadRepository.findByFollowUpDate(today);

        for (Lead lead : leadsToFollowUp) {
            if (!lead.getConverted()) {
                notificationService.broadcast("LEAD_FOLLOWUP", Map.of(
                        "leadId", lead.getId(),
                        "name", lead.getName(),
                        "mobile", lead.getMobile(),
                        "interestedPlan", lead.getInterestedPlan()
                ));
            }
        }
    }
}
