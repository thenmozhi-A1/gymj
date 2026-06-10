package com.example.gym.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bound from application.properties prefix "gym.notifications".
 * Toggle enabled/disabled and configure days-ahead window at runtime
 * via environment variables or the admin settings API.
 */
@ConfigurationProperties(prefix = "gym.notifications")
public class NotificationSettings {

    /** Master on/off switch — false by default until SMTP is configured */
    private boolean enabled = false;

    /** How many days before plan expiry to send the reminder */
    private int expiryDaysAhead = 7;

    /** From-address used in outgoing emails */
    private String fromEmail = "noreply@byfitness.com";

    /** Display name shown in email headers */
    private String gymName = "B&Y Fitness Gym";

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public boolean isEnabled()                   { return enabled; }
    public void setEnabled(boolean enabled)      { this.enabled = enabled; }

    public int getExpiryDaysAhead()              { return expiryDaysAhead; }
    public void setExpiryDaysAhead(int d)        { this.expiryDaysAhead = d; }

    public String getFromEmail()                 { return fromEmail; }
    public void setFromEmail(String fromEmail)   { this.fromEmail = fromEmail; }

    public String getGymName()                   { return gymName; }
    public void setGymName(String gymName)       { this.gymName = gymName; }
}
