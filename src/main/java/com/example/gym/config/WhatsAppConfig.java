package com.example.gym.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bound from application.properties prefix "whatsapp".
 * Toggle enabled/disabled and configure WhatsApp Business Cloud API
 * credentials via environment variables.
 *
 * Credentials are NEVER hardcoded — they must be set via:
 *   WHATSAPP_ENABLED, WHATSAPP_API_URL, WHATSAPP_PHONE_NUMBER_ID,
 *   WHATSAPP_ACCESS_TOKEN, WHATSAPP_BUSINESS_ACCOUNT_ID
 */
@ConfigurationProperties(prefix = "whatsapp")
public class WhatsAppConfig {

    /** Master on/off switch — false by default until credentials are configured */
    private boolean enabled = false;

    /** Meta Graph API base URL */
    private String apiUrl = "https://graph.facebook.com/v21.0";

    /** WhatsApp Phone Number ID from Meta Business Manager */
    private String phoneNumberId = "";

    /** Permanent access token for the WhatsApp Business API */
    private String accessToken = "";

    /** WhatsApp Business Account ID */
    private String businessAccountId = "";

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public boolean isEnabled()                          { return enabled; }
    public void setEnabled(boolean enabled)             { this.enabled = enabled; }

    public String getApiUrl()                           { return apiUrl; }
    public void setApiUrl(String apiUrl)                { this.apiUrl = apiUrl; }

    public String getPhoneNumberId()                    { return phoneNumberId; }
    public void setPhoneNumberId(String phoneNumberId)  { this.phoneNumberId = phoneNumberId; }

    public String getAccessToken()                      { return accessToken; }
    public void setAccessToken(String accessToken)      { this.accessToken = accessToken; }

    public String getBusinessAccountId()                        { return businessAccountId; }
    public void setBusinessAccountId(String businessAccountId)  { this.businessAccountId = businessAccountId; }
}
