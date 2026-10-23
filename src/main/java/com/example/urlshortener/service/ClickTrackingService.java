package com.example.urlshortener.service;

import com.example.urlshortener.domain.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClickTrackingService {
    private final UrlRepository repository;
    private final ClickEventRepository events;
    public ClickTrackingService(UrlRepository repository, ClickEventRepository events) { this.repository = repository; this.events = events; }
    @Async
    @Transactional
    public void recordClick(String shortCode) { recordClick(shortCode, null, null, null); }
    @Async
    @Transactional
    public void recordClick(String shortCode, String referrer, String userAgent, String clientIp) {
        UrlMapping mapping = repository.findByShortCode(shortCode).orElse(null);
        if (mapping == null) return;
        repository.incrementClickCount(shortCode);
        events.save(new ClickEvent(mapping, referrerSource(referrer), deviceType(userAgent), visitorHash(clientIp, userAgent)));
    }
    private String referrerSource(String referrer) { try { return referrer == null || referrer.isBlank() ? "Direct / unknown" : URI.create(referrer).getHost(); } catch (IllegalArgumentException ex) { return "Direct / unknown"; } }
    private String deviceType(String agent) { String value = agent == null ? "" : agent.toLowerCase(); if (value.contains("ipad") || value.contains("tablet")) return "Tablet"; if (value.contains("mobi") || value.contains("android")) return "Mobile"; return "Desktop"; }
    private String visitorHash(String ip, String agent) { try { byte[] digest = MessageDigest.getInstance("SHA-256").digest((LocalDate.now() + "|" + (ip == null ? "" : ip) + "|" + (agent == null ? "" : agent)).getBytes(StandardCharsets.UTF_8)); StringBuilder output = new StringBuilder(); for (byte b : digest) output.append(String.format("%02x", b)); return output.toString(); } catch (Exception ex) { return null; } }
}
