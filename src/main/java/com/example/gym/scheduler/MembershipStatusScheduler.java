package com.example.gym.scheduler;

import com.example.gym.entity.Payment;
import com.example.gym.entity.User;
import com.example.gym.repository.PaymentRepository;
import com.example.gym.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class MembershipStatusScheduler {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public MembershipStatusScheduler(UserRepository userRepository, PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Runs every day at midnight.
     * Finds users whose latest payment's planEndDate is before now
     * and sets their status to EXPIRED.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void updateExpiredMemberships() {
        LocalDateTime now = LocalDateTime.now();
        List<User> activeUsers = userRepository.findByStatus("ACTIVE");

        for (User user : activeUsers) {
            Optional<Payment> latestPayment = paymentRepository.findAll().stream()
                    .filter(p -> p.getUser().getId().equals(user.getId()) && "SUCCESS".equalsIgnoreCase(p.getPaymentStatus()))
                    .max((p1, p2) -> p1.getPlanEndDate().compareTo(p2.getPlanEndDate()));

            if (latestPayment.isPresent() && latestPayment.get().getPlanEndDate().isBefore(now)) {
                user.setStatus("EXPIRED");
                userRepository.save(user);
            }
        }
    }
}
