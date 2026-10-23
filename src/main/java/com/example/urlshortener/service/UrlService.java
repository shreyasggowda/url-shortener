package com.example.urlshortener.service;

import com.example.urlshortener.domain.*;
import java.net.URI;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlService {
    private final UrlRepository repository;
    private final Base62Encoder encoder;
    private final UrlCache cache;
    private final ClickTrackingService clickTracking;
    public UrlService(UrlRepository repository, Base62Encoder encoder, UrlCache cache, ClickTrackingService clickTracking) { this.repository = repository; this.encoder = encoder; this.cache = cache; this.clickTracking = clickTracking; }
    @Transactional
    public UrlMapping create(String longUrl, String customAlias, Instant expiresAt, AppUser owner) {
        validateDestination(longUrl);
        if (expiresAt != null && !expiresAt.isAfter(Instant.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiration must be in the future");
        String alias = normalizeAlias(customAlias);
        if (alias != null && repository.findByShortCode(alias).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "That custom alias is already in use");
        long id = repository.nextId();
        UrlMapping mapping = new UrlMapping(id, longUrl, owner);
        mapping.setShortCode(alias == null ? encoder.encode(id) : alias);
        mapping.setExpiresAt(expiresAt);
        mapping = repository.save(mapping);
        cache.put(mapping.getShortCode(), longUrl, expiresAt);
        return mapping;
    }
    public String resolve(String shortCode) {
        String longUrl = cache.get(shortCode);
        if (longUrl == null) {
            UrlMapping mapping = repository.findByShortCode(shortCode).orElseThrow(() -> new UrlNotFoundException(shortCode));
            if (mapping.isExpired()) throw new UrlNotFoundException(shortCode);
            longUrl = mapping.getLongUrl();
            cache.put(shortCode, longUrl, mapping.getExpiresAt());
        }
        clickTracking.recordClick(shortCode);
        return longUrl;
    }
    @Transactional(readOnly = true)
    public UrlMapping stats(String shortCode, AppUser user) {
        UrlMapping mapping = repository.findByShortCode(shortCode).orElseThrow(() -> new UrlNotFoundException(shortCode));
        if (mapping.getOwner() == null || !mapping.getOwner().getId().equals(user.getId())) throw new org.springframework.security.access.AccessDeniedException("You do not have access to this link's statistics");
        return mapping;
    }
    @Transactional(readOnly = true)
    public Page<UrlMapping> list(AppUser user, String search, String status, Instant fromDate, Instant toDate, Pageable pageable) {
        String safeStatus = switch (status == null ? "all" : status) { case "all", "active", "expired" -> status == null ? "all" : status; default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be all, active, or expired"); };
        return repository.findOwnedLinks(user.getId(), blankToNull(search), safeStatus, fromDate, toDate, pageable);
    }
    @Transactional
    public void delete(String shortCode, AppUser user) {
        UrlMapping mapping = stats(shortCode, user);
        repository.delete(mapping);
        cache.delete(shortCode);
    }
    private String normalizeAlias(String alias) {
        if (alias == null || alias.isBlank()) return null;
        String normalized = alias.trim();
        if (!normalized.matches("[A-Za-z0-9]{3,10}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom alias must be 3–10 letters or numbers");
        return normalized;
    }
    private void validateDestination(String url) {
        try { String scheme = URI.create(url).getScheme(); if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) throw new IllegalArgumentException(); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid HTTP or HTTPS URL, including https://"); }
    }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
