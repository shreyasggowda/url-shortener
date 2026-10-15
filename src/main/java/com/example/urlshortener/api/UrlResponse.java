package com.example.urlshortener.api;

import com.example.urlshortener.domain.UrlMapping;
import java.time.Instant;

public record UrlResponse(String shortCode, String shortUrl, String longUrl, long clickCount, Instant createdAt) {
    public static UrlResponse from(UrlMapping mapping, String baseUrl) {
        return new UrlResponse(mapping.getShortCode(), baseUrl + "/" + mapping.getShortCode(), mapping.getLongUrl(), mapping.getClickCount(), mapping.getCreatedAt());
    }
}
