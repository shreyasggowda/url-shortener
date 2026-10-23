package com.example.urlshortener.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "click_events")
public class ClickEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "url_id", nullable = false) private UrlMapping url;
    @Column(name = "clicked_at", nullable = false) private Instant clickedAt = Instant.now();
    @Column(name = "referrer_source", length = 254) private String referrerSource;
    @Column(name = "device_type", nullable = false, length = 16) private String deviceType;
    @Column(name = "visitor_hash", length = 64) private String visitorHash;
    protected ClickEvent() { }
    public ClickEvent(UrlMapping url, String referrerSource, String deviceType, String visitorHash) { this.url = url; this.referrerSource = referrerSource; this.deviceType = deviceType; this.visitorHash = visitorHash; }
}
