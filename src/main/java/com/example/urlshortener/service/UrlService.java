package com.example.urlshortener.service;

import com.example.urlshortener.domain.*;
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
    public UrlMapping create(String longUrl, AppUser owner) {
        long id = repository.nextId();
        UrlMapping mapping = new UrlMapping(id, longUrl, owner);
        mapping.setShortCode(encoder.encode(id));
        mapping = repository.save(mapping);
        cache.put(mapping.getShortCode(), longUrl);
        return mapping;
    }
    public String resolve(String shortCode) {
        String longUrl = cache.get(shortCode);
        if (longUrl == null) {
            UrlMapping mapping = repository.findByShortCode(shortCode).orElseThrow(() -> new UrlNotFoundException(shortCode));
            longUrl = mapping.getLongUrl();
            cache.put(shortCode, longUrl);
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
}
