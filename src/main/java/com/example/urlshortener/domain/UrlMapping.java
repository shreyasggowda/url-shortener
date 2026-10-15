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
    @Column(name = "api_key_owner", nullable = false, length = 100)
    private String apiKeyOwner;
    @Column(name = "click_count", nullable = false)
    private long clickCount;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UrlMapping() { }
    public UrlMapping(Long id, String longUrl, String apiKeyOwner) { this.id = id; this.longUrl = longUrl; this.apiKeyOwner = apiKeyOwner; this.createdAt = Instant.now(); }
    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getLongUrl() { return longUrl; }
    public long getClickCount() { return clickCount; }
    public Instant getCreatedAt() { return createdAt; }
}
