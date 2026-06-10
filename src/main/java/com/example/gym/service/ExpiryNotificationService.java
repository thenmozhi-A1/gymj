package com.example.gym.service;

import com.example.gym.config.NotificationSettings;
import com.example.gym.entity.Payment;
import com.example.gym.entity.User;
import com.example.gym.repository.PaymentRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ExpiryNotificationService
 *
 * Runs once daily at 08:00 and scans all payments whose planEndDate falls
 * within the configured window (default 7 days). For each unique member in that
 * set, it sends a branded HTML email reminder.
 *
 * The job is controlled by gym.notifications.enabled:
 *   - false (default) → job runs but skips sending (safe with no SMTP config).
 *   - true            → sends real email; requires SMTP env vars.
 *
 * Idempotent: deduplicates by user ID so a member with multiple plans in the
 * window only gets one email per run.
 */
@Service
public class ExpiryNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ExpiryNotificationService.class);
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final PaymentRepository paymentRepository;
    private final JavaMailSender mailSender;
    private final NotificationSettings settings;

    public ExpiryNotificationService(PaymentRepository paymentRepository,
                                     JavaMailSender mailSender,
                                     NotificationSettings settings) {
        this.paymentRepository = paymentRepository;
        this.mailSender        = mailSender;
        this.settings          = settings;
    }

    /**
     * Scheduled trigger — runs every day at 08:00 server time.
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendExpiryReminders() {
        if (!settings.isEnabled()) {
            log.info("[ExpiryJob] Notifications disabled — skipping run.");
            return;
        }

        int daysAhead = settings.getExpiryDaysAhead();
        LocalDateTime windowStart = LocalDateTime.now();
        LocalDateTime windowEnd   = windowStart.plusDays(daysAhead);

        log.info("[ExpiryJob] Scanning payments expiring between {} and {} ...",
                windowStart.toLocalDate(), windowEnd.toLocalDate());

        List<Payment> expiringPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getPlanEndDate() != null
                          && p.getPlanEndDate().isAfter(windowStart)
                          && p.getPlanEndDate().isBefore(windowEnd)
                          && "SUCCESS".equalsIgnoreCase(p.getPaymentStatus()))
                .toList();

        // Deduplicate — one email per user even if they have multiple plans expiring
        Set<Long> notified = new HashSet<>();
        int sent = 0;

        for (Payment payment : expiringPayments) {
            User user = payment.getUser();
            if (user == null || user.getId() == null) continue;
            if (notified.contains(user.getId()))       continue;

            try {
                sendReminderEmail(user, payment);
                notified.add(user.getId());
                sent++;
                log.info("[ExpiryJob] Sent reminder to {} (expires {})",
                        user.getEmail(), payment.getPlanEndDate().toLocalDate());
            } catch (Exception e) {
                log.error("[ExpiryJob] Failed to send email to {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("[ExpiryJob] Done. Sent {} reminder(s) for {} expiring payment(s).", sent, expiringPayments.size());
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

    // ── Email composition ─────────────────────────────────────────────────────

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
