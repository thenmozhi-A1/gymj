package com.example.gym.service;

import com.example.gym.config.NotificationSettings;
import com.example.gym.entity.Payment;
import com.example.gym.entity.User;
import com.example.gym.repository.PaymentRepository;
import com.example.gym.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Map;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ExpiryNotificationService
 *
 * Runs once daily at 08:00 and scans BOTH the Users table (user.expiryDate)
 * and the Payments table (payment.planEndDate) for memberships expiring within
 * the configured window (default 7 days). Sends both email and WhatsApp reminders.
 *
 * The job is controlled by gym.notifications.enabled:
 *   - false (default) → job runs but skips sending (safe with no SMTP config).
 *   - true            → sends real email + WhatsApp; requires credentials.
 *
 * Idempotent: deduplicates by user ID so a member only gets one notification per run.
 */
@Service
public class ExpiryNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ExpiryNotificationService.class);
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final NotificationSettings settings;
    private final WhatsAppService whatsAppService;

    public ExpiryNotificationService(PaymentRepository paymentRepository,
                                     UserRepository userRepository,
                                     JavaMailSender mailSender,
                                     NotificationSettings settings,
                                     WhatsAppService whatsAppService) {
        this.paymentRepository = paymentRepository;
        this.userRepository    = userRepository;
        this.mailSender        = mailSender;
        this.settings          = settings;
        this.whatsAppService   = whatsAppService;
    }

    /**
     * Scheduled trigger — runs every day at 08:00 server time.
     * Scans BOTH the Users table (user.expiryDate) and the Payments table
     * (payment.planEndDate) for memberships expiring within the configured window.
     * Sends both email and WhatsApp reminders for each matching member.
     *
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendExpiryReminders() {
        if (!settings.isEnabled()) {
            log.info("[ExpiryJob] Notifications disabled — skipping run.");
            return;
        }

        int daysAhead = settings.getExpiryDaysAhead();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusDays(daysAhead);

        log.info("[ExpiryJob] Scanning memberships expiring between {} and {} ...",
                now.toLocalDate(), windowEnd.toLocalDate());

        // Track notified users to avoid duplicates across both scans
        Set<Long> notified = new HashSet<>();
        int emailsSent = 0;
        int whatsappSent = 0;

        // ── Pass 1: Scan Users table (user.expiryDate field) ──────────────────
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (user.getId() == null || user.getExpiryDate() == null || user.getExpiryDate().isBlank()) continue;
            if (notified.contains(user.getId())) continue;

            try {
                LocalDateTime expiryDateObj = parseExpiryDate(user.getExpiryDate());
                if (expiryDateObj == null) continue;

                // Check if within the notification window
                if (expiryDateObj.isAfter(now) && expiryDateObj.isBefore(windowEnd)) {
                    String planName = user.getMembershipPlan() != null ? user.getMembershipPlan() : "Gym Membership";
                    String expiryDateStr = expiryDateObj.format(DISPLAY_FMT);
                    int daysLeft = (int) java.time.temporal.ChronoUnit.DAYS.between(now, expiryDateObj);

                    // Send email
                    try {
                        sendReminderEmailForUser(user, planName, expiryDateStr, daysLeft);
                        emailsSent++;
                        log.info("[ExpiryJob] Email sent to {} (expires {})", user.getEmail(), expiryDateObj.toLocalDate());
                    } catch (Exception e) {
                        log.error("[ExpiryJob] Email failed for {}: {}", user.getEmail(), e.getMessage());
                    }

                    // Send WhatsApp — independent of email success
                    try {
                        whatsAppService.sendExpiryReminder(user, planName, expiryDateStr, daysLeft);
                        whatsappSent++;
                    } catch (Exception e) {
                        log.error("[ExpiryJob] WhatsApp failed for user {}: {}", user.getId(), e.getMessage());
                    }

                    notified.add(user.getId());
                }
            } catch (Exception e) {
                log.warn("[ExpiryJob] Error processing user {}: {}", user.getId(), e.getMessage());
            }
        }

        // ── Pass 2: Scan Payments table (for users not caught in Pass 1) ──────
        List<Payment> expiringPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getPlanEndDate() != null
                          && p.getPlanEndDate().isAfter(now)
                          && p.getPlanEndDate().isBefore(windowEnd)
                          && "SUCCESS".equalsIgnoreCase(p.getPaymentStatus()))
                .toList();

        for (Payment payment : expiringPayments) {
            User user = payment.getUser();
            if (user == null || user.getId() == null) continue;
            if (notified.contains(user.getId())) continue;

            String planName = payment.getPlanName() != null ? payment.getPlanName() : "Gym Membership";
            String expiryDate = payment.getPlanEndDate().format(DISPLAY_FMT);
            int daysLeft = (int) java.time.temporal.ChronoUnit.DAYS.between(now, payment.getPlanEndDate());

            // Send email
            try {
                sendReminderEmail(user, payment);
                emailsSent++;
                log.info("[ExpiryJob] Email sent to {} via payment (expires {})",
                        user.getEmail(), payment.getPlanEndDate().toLocalDate());
            } catch (Exception e) {
                log.error("[ExpiryJob] Email failed for {}: {}", user.getEmail(), e.getMessage());
            }

            // Send WhatsApp — independent of email success
            try {
                whatsAppService.sendExpiryReminder(user, planName, expiryDate, daysLeft);
                whatsappSent++;
            } catch (Exception e) {
                log.error("[ExpiryJob] WhatsApp failed for user {}: {}", user.getId(), e.getMessage());
            }

            notified.add(user.getId());
        }

        log.info("[ExpiryJob] Done. Notified {} member(s) — {} email(s), {} WhatsApp message(s).",
                notified.size(), emailsSent, whatsappSent);
    }

    /**
     * Parses the user's expiryDate string (can be "2025-07-01" or "2025-07-01T00:00:00").
     */
    private LocalDateTime parseExpiryDate(String dateStr) {
        try {
            if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr);
            } else {
                return java.time.LocalDate.parse(dateStr).atStartOfDay();
            }
        } catch (Exception e) {
            log.warn("[ExpiryJob] Cannot parse expiry date '{}': {}", dateStr, e.getMessage());
            return null;
        }
    }

    /**
     * Sends a branded HTML email reminder using data from the User entity directly.
     */
    private void sendReminderEmailForUser(User user, String planName, String expiryDate, int daysLeft) throws Exception {
        String recipientName = user.getFullName() != null ? user.getFullName() : "Member";
        String gymName = settings.getGymName();

        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

        helper.setFrom(settings.getFromEmail(), gymName);
        helper.setTo(user.getEmail());
        helper.setSubject("⏰ Your " + gymName + " membership expires in " + daysLeft + " day" + (daysLeft == 1 ? "" : "s"));
        helper.setText(buildHtml(recipientName, planName, expiryDate, daysLeft, gymName), true);

        mailSender.send(msg);
    }

    /**
     * Exposed for the admin settings API to trigger a manual test run.
     * Returns the count of emails sent.
     */
    public int triggerManualRun() {
        boolean wasEnabled = settings.isEnabled();
        settings.setEnabled(true);
        sendExpiryReminders();
        settings.setEnabled(wasEnabled);
        return 0; // count is logged; returning 0 as placeholder
    }

    /**
     * Sends a manual reminder via WhatsApp only (Email is handled by EmailJS on frontend).
     *
     * @return a result map with keys: whatsappSent, errors
     */
    public Map<String, Object> sendManualReminder(User user) {
        String recipientName  = user.getFullName() != null ? user.getFullName() : "Member";
        
        LocalDateTime expiryDateObj = LocalDateTime.now();
        if (user.getExpiryDate() != null) {
            try {
                if (user.getExpiryDate().contains("T")) {
                    expiryDateObj = LocalDateTime.parse(user.getExpiryDate());
                } else {
                    expiryDateObj = java.time.LocalDate.parse(user.getExpiryDate()).atStartOfDay();
                }
            } catch (Exception e) {
                log.warn("Failed to parse expiry date: " + user.getExpiryDate(), e);
            }
        }
        String expiryDate     = expiryDateObj.format(DISPLAY_FMT);
        String planName       = user.getMembershipPlan() != null ? user.getMembershipPlan() : "Gym Membership";
        int    daysLeft       = (int) java.time.temporal.ChronoUnit.DAYS.between(
                                    LocalDateTime.now(), expiryDateObj);

        boolean whatsappSent = false;
        java.util.ArrayList<String> errors = new java.util.ArrayList<>();

        // ── Try WhatsApp ──────────────────────────────────────────────────────
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            try {
                whatsAppService.sendExpiryReminder(user, planName, expiryDate, daysLeft);
                whatsappSent = true;
                log.info("[ManualReminder] WhatsApp sent to user {}", user.getId());
            } catch (Exception e) {
                log.error("[ManualReminder] WhatsApp failed for user {}: {}", user.getId(), e.getMessage());
                errors.add("WhatsApp failed: " + e.getMessage());
            }
        } else {
            errors.add("WhatsApp failed: User has no phone number");
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("whatsappSent", whatsappSent);
        result.put("errors", errors);
        return result;
    }

    private void sendReminderEmail(User user, Payment payment) throws Exception {
        String recipientName  = user.getFullName() != null ? user.getFullName() : "Member";
        String recipientEmail = user.getEmail();
        String expiryDate     = payment.getPlanEndDate().format(DISPLAY_FMT);
        String planName       = payment.getPlanName() != null ? payment.getPlanName() : "Gym Membership";
        String gymName        = settings.getGymName();
        int    daysLeft       = (int) java.time.temporal.ChronoUnit.DAYS.between(
                                    LocalDateTime.now(), payment.getPlanEndDate());

        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

        helper.setFrom(settings.getFromEmail(), gymName);
        helper.setTo(recipientEmail);
        helper.setSubject("⏰ Your " + gymName + " membership expires in " + daysLeft + " day" + (daysLeft == 1 ? "" : "s"));
        helper.setText(buildHtml(recipientName, planName, expiryDate, daysLeft, gymName), true);

        mailSender.send(msg);
    }

    private String buildHtml(String name, String plan, String expiryDate, int daysLeft, String gymName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8"/>
              <style>
                body { font-family: 'Segoe UI', sans-serif; background:#0f172a; margin:0; padding:0; }
                .wrapper { max-width:560px; margin:40px auto; background:#1e293b; border-radius:16px; overflow:hidden; border:1px solid #334155; }
                .header  { background:linear-gradient(135deg,#1e293b,#0f172a); padding:36px 40px; text-align:center; border-bottom:3px solid #facc15; }
                .header h1 { color:#facc15; font-size:22px; margin:0 0 6px 0; letter-spacing:1px; }
                .header p  { color:#94a3b8; margin:0; font-size:13px; }
                .body    { padding:32px 40px; }
                .body p  { color:#cbd5e1; font-size:15px; line-height:1.7; margin:0 0 16px 0; }
                .highlight { background:#0f172a; border:1px solid #334155; border-radius:10px; padding:18px 20px; margin:20px 0; }
                .highlight .label { font-size:11px; color:#64748b; text-transform:uppercase; letter-spacing:0.5px; margin-bottom:4px; }
                .highlight .value { font-size:18px; font-weight:700; color:#facc15; }
                .cta { display:block; text-align:center; margin:28px 0 8px; padding:14px 28px; background:linear-gradient(135deg,#facc15,#f59e0b); color:#000; font-weight:800; font-size:15px; border-radius:10px; text-decoration:none; }
                .footer  { padding:20px 40px; text-align:center; border-top:1px solid #334155; }
                .footer p { color:#475569; font-size:12px; margin:0; }
              </style>
            </head>
            <body>
              <div class="wrapper">
                <div class="header">
                  <h1>🏋️ %s</h1>
                  <p>Elite Fitness Operations Center</p>
                </div>
                <div class="body">
                  <p>Hi <strong style="color:#f1f5f9">%s</strong>,</p>
                  <p>Your <strong style="color:#f1f5f9">%s</strong> is expiring soon. Don't let your fitness streak stop — renew now and keep the momentum going!</p>
                  <div class="highlight">
                    <div class="label">Plan Expiry Date</div>
                    <div class="value">%s</div>
                  </div>
                  <div class="highlight">
                    <div class="label">Days Remaining</div>
                    <div class="value">%d day%s</div>
                  </div>
                  <a class="cta" href="#">Renew My Membership Now →</a>
                  <p style="font-size:13px;color:#64748b">If you have already renewed, please ignore this email. For help, reply to this message or visit the gym front desk.</p>
                </div>
                <div class="footer">
                  <p>© %s. This is an automated reminder — please do not reply directly.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(gymName, name, plan, expiryDate, daysLeft, daysLeft == 1 ? "" : "s", gymName);
    }
}
