package com.example.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "webauthn_credentials")
public class WebAuthnCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 1024)
    private String credentialId;

    @Lob
    @Column(nullable = false, columnDefinition = "BLOB")
    private byte[] publicKeyCose;

    @Column(nullable = false)
    private long signCount;

    @Column(length = 255)
    private String aaguid;

    @Column(length = 255)
    private String deviceType;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    private Boolean active = true;

    public WebAuthnCredential() {}

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastUsedAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }
    public byte[] getPublicKeyCose() { return publicKeyCose; }
    public void setPublicKeyCose(byte[] publicKeyCose) { this.publicKeyCose = publicKeyCose; }
    public long getSignCount() { return signCount; }
    public void setSignCount(long signCount) { this.signCount = signCount; }
    public String getAaguid() { return aaguid; }
    public void setAaguid(String aaguid) { this.aaguid = aaguid; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
