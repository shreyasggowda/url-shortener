package com.example.urlshortener.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "urls")
public class UrlMapping {
    @Id
    private Long id;
    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;
    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    private String longUrl;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private AppUser owner;
    @Column(name = "click_count", nullable = false)
    private long clickCount;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "expires_at")
    private Instant expiresAt;

    protected UrlMapping() { }
    public UrlMapping(Long id, String longUrl, AppUser owner) { this.id = id; this.longUrl = longUrl; this.owner = owner; this.createdAt = Instant.now(); }
    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getLongUrl() { return longUrl; }
    public AppUser getOwner() { return owner; }
    public long getClickCount() { return clickCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isExpired() { return expiresAt != null && !expiresAt.isAfter(Instant.now()); }
}
